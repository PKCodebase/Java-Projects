package com.mcd.plantation.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.mcd.plantation.config.MasterDataCache;
import com.mcd.plantation.dto.request.CitizenApiRequest;
import com.mcd.plantation.dto.response.UserSummaryResponse;
import com.mcd.plantation.dto.response.OfficerMasterResponse;
import com.mcd.plantation.entity.Citizen;
import com.mcd.plantation.entity.McdOfficial;
import com.mcd.plantation.enums.OfficialRole;
import com.mcd.plantation.enums.UserTypeCode;
import com.mcd.plantation.repository.CitizenRepository;
import com.mcd.plantation.repository.McdOfficialRepository;
import com.mcd.plantation.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Client for the external MCD "Swagam" master-data service.
 *
 * <p>Every call now has a <b>local fallback</b> against the {@code citizens}
 * and {@code mcd_officials} tables, so the three portals keep working when the
 * external service is unreachable (offline, staging down, no egress): names,
 * zone scoping and official listings resolve from the database instead of
 * failing with a 500. No exception message, token or credential is ever
 * written to the log.</p>
 */
@Service @RequiredArgsConstructor @Slf4j
public class SwagamService {

	@Autowired
    private RestTemplate restTemplate;
    
    @Value("${swagam.api-base-url}")
    private String baseUrl;
    
    private final MasterDataCache masterDataCache;
    private final CitizenRepository citizenRepo;
    private final McdOfficialRepository officialRepo;
    private final JwtUtil jwtUtil;

	/*
	 * public SwagamService(RestTemplate restTemplate,
	 * 
	 * @Value("${service.api-base-url}") String baseUrl) { this.restTemplate =
	 * restTemplate; this.baseUrl = baseUrl; }
	 */

    public String getWrapperCode(String roleCode, String token) {
    	
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/admin/user/s/role/group")
                .queryParam("rc", roleCode)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token); 
        headers.set("X-SESS-NONCE", UUID.randomUUID().toString()); 

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> body = response.getBody();
             
            if (body != null && !body.isEmpty()) {
                Object wrapperCode = body.get(0).get("wrapperCode");
                return wrapperCode != null ? wrapperCode.toString() : null;
            }
        } catch (Exception e) {
            // External service unavailable — caller treats null as "unknown".
            return null;
        }
        return null;
    }
    
    
    public List<OfficerMasterResponse> getUsersByRg(
            String token, 
            String utc, 
            String rc, 
            String p, 
            String w, //wrapperCode
            String o) {

        try {
            UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat("/admin/user/by/rg"))
                    .queryParam("utc", utc)
                    .queryParam("rc", rc);

            Optional.ofNullable(p).ifPresent(val -> urlBuilder.queryParam("p", val));
            Optional.ofNullable(w).ifPresent(val -> urlBuilder.queryParam("w", val));
            Optional.ofNullable(o).ifPresent(val -> urlBuilder.queryParam("o", val));

            String finalUrl = urlBuilder.toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-SESS-NONCE", UUID.randomUUID().toString()); 
            headers.setBearerAuth(token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<OfficerMasterResponse>> response = restTemplate.exchange(
                    finalUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<OfficerMasterResponse>>() {}
            );

            List<OfficerMasterResponse> body = response.getBody();
            return body != null ? body : localOfficials(rc, w);
        } catch (Exception ex) {
            return localOfficials(rc, w);
        }
    }
    
    /**
     * Returns the display summary for a user. The local credential tables are
     * checked first (they are the source of truth for accounts created in this
     * application and need no network round-trip per row), the external
     * service is only consulted for users that are not known locally.
     */
    public UserSummaryResponse fetchUserSummary(String userCode, String userTypeCode) {

    	UserSummaryResponse local = localUserSummary(userCode, userTypeCode);
    	if (local != null) {
    		return local;
    	}

    	try {
	        String url = baseUrl.concat("/IntApis/user/info");

	        CitizenApiRequest requestBody = new CitizenApiRequest(
	                userCode,
	                "",       
	                "",       
	                "",       // mobileNumber
	                "",       // emailId
	                userTypeCode,
	                ""        // modifiedDate
	        );

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        headers.set("X-SESS-NONCE", UUID.randomUUID().toString()); 
	        HttpEntity<CitizenApiRequest> entity = new HttpEntity<>(requestBody, headers);

	        ResponseEntity<CitizenApiResponse> responseEntity = restTemplate.exchange(
	                url,
	                HttpMethod.POST,
	                entity,
	                CitizenApiResponse.class
	        );

	        CitizenApiResponse apiResponse = responseEntity.getBody();
	        if (apiResponse != null) {
	            return new UserSummaryResponse(
	                    apiResponse.userSystemCode(),
	                    apiResponse.username(),       
	                    apiResponse.emailId(),        
	                    apiResponse.mobileNumber(),
	                    "",
	                    "",
	                    true,
	                    null,
	                    null
	            );
	        }
    	} catch (Exception ex) {
    		// fall through to the identifier-based fallback below
    	}

        return new UserSummaryResponse(
                Objects.toString(userCode, ""),
                Objects.toString(userCode, ""),
                "", "", null, null, true, null, null);
    }
    
    
    public Optional<OfficerMasterResponse> findFirstByCode(List<OfficerMasterResponse> officers, String targetCode) {
        if (officers == null || targetCode == null) {
            return Optional.empty();
        }
        return officers.stream()
                .filter(officer -> targetCode.equals(officer.userCode()))
                .findFirst();
    }
    
    /**
     * Zone the calling official works in (returned as the zone UUID, which is
     * what {@code ParkService} compares a park's zone against).
     *
     * <p>The official's own row in {@code mcd_officials} is authoritative and
     * is read from the caller's token, so this works without the external
     * service; the external lookup remains as a fallback.</p>
     */
    public String getOfficialZoneCode(
            String token, String userTypeCode, String roleCode, 
            String p, String w, String o) {
             
        String localZone = localOfficialZone(token);
        if (localZone != null) {
            return localZone;
        }

        List<OfficerMasterResponse> users = getUsersByRg(token, userTypeCode, roleCode, p, w, o);
        
        if (users != null && !users.isEmpty()) {
            for (OfficerMasterResponse user : users) {
                if (user.wrapperOrgCode() != null && !user.wrapperOrgCode().isBlank()) {
                	String zoneCode = masterDataCache.getZoneCodeForWrapperCode(user.wrapperOrgCode());
                	if (zoneCode != null && !zoneCode.isBlank()) {
                		return zoneCode;
                	}
                }
            }
        }
        return null; 
    }
    
    // ── local fallbacks ────────────────────────────────────────────────

    /**
     * Officials from the local {@code mcd_officials} table, mapped onto the
     * same shape the external service returns.
     *
     * @param rc role code ({@code R_HORTIC_ADM} / {@code R_HORTIC_OFF} /
     *           {@code SUPERVISOR}), {@code null} for "all roles"
     * @param zoneId zone UUID to restrict the list to, or {@code null}
     */
    private List<OfficerMasterResponse> localOfficials(String rc, String zoneId) {
        List<McdOfficial> officials;

        OfficialRole role = null;
        if (rc != null && !rc.isBlank()) {
            try {
                role = OfficialRole.valueOf(rc.trim());
            } catch (IllegalArgumentException ignored) {
                role = null;
            }
        }

        officials = (role != null)
                ? officialRepo.findByRoleOrderByName(role)
                : officialRepo.findAllByOrderByName();

        final String wantedZone = (zoneId == null || zoneId.isBlank()) ? null : zoneId;

        return officials.stream()
                .filter(McdOfficial::isActive)
                .filter(o -> wantedZone == null || wantedZone.equals(zoneIdOf(o)))
                .map(o -> new OfficerMasterResponse(
                        o.getEmployeeId(),
                        o.getName(),
                        null,
                        zoneIdOf(o),
                        null))
                .toList();
    }

    /**
     * Local look-up of a user summary; returns {@code null} when the code is
     * not known locally (caller then tries the external service).
     */
    private UserSummaryResponse localUserSummary(String userCode, String userTypeCode) {
        if (userCode == null || userCode.isBlank()) {
            return null;
        }

        if (!UserTypeCode.UT_EMP.name().equals(userTypeCode)) {
            Optional<Citizen> citizen = findCitizen(userCode);
            if (citizen.isPresent()) {
                Citizen c = citizen.get();
                return new UserSummaryResponse(
                        c.getCitizenId().toString(),
                        c.getFullName(),
                        c.getEmail(),
                        c.getPhone(),
                        c.getAadhaarLast4(),
                        c.getAddress(),
                        c.isActive(),
                        c.getRegisteredAt(),
                        c.getLastLoginAt());
            }
        }

        Optional<McdOfficial> official = findOfficial(userCode);
        if (official.isPresent()) {
            McdOfficial m = official.get();
            return new UserSummaryResponse(
                    m.getOfficialId(),
                    m.getName(),
                    m.getEmail(),
                    m.getPhone(),
                    null,
                    null,
                    m.isActive(),
                    m.getCreatedAt(),
                    null);
        }

        return null;
    }

    /**
     * Zone of the official identified by the caller's own token.
     */
    private String localOfficialZone(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            String systemCode = jwtUtil.getUserToken(token).getUserSystemCode();
            return findOfficial(systemCode)
                    .map(SwagamService::zoneIdOf)
                    .orElse(null);
        } catch (Exception ex) {
            return null;
        }
    }

    private Optional<Citizen> findCitizen(String userCode) {
        try {
            return citizenRepo.findById(UUID.fromString(userCode.trim()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private Optional<McdOfficial> findOfficial(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String trimmed = code.trim();
        Optional<McdOfficial> byId = officialRepo.findById(trimmed);
        if (byId.isPresent()) {
            return byId;
        }
        return officialRepo.findByEmployeeId(trimmed).stream().findFirst();
    }

    private static String zoneIdOf(McdOfficial official) {
        return official.getZoneId() == null ? null : official.getZoneId().toString();
    }

	@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
	private record CitizenApiResponse(
	    String userSystemCode,
	    String username,
	    String emailId,
	    String mobileNumber
	) {}
    
    
}
