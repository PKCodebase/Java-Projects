package com.mcd.plantation.service.impl;

import com.mcd.plantation.config.MasterDataCache;
import com.mcd.plantation.dto.request.*;
import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.entity.*;
import com.mcd.plantation.enums.OfficialRole;
import com.mcd.plantation.exception.DuplicateResourceException;
import com.mcd.plantation.exception.ResourceNotFoundException;
import com.mcd.plantation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class UserManagementService {

    private final McdOfficialRepository officialRepo;
    private final CitizenRepository citizenRepo;
    //private final ZoneRepository zoneRepo;
    //private final WardRepository wardRepo;
    //private final PasswordEncoder passwordEncoder;
    private final MasterDataCache masterDataCache;
    private final SwagamService swagamService;

    // ── Officials ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<OfficialDetailResponse> getAllOfficials(String token, OfficialRole role, Boolean active) {
    	
    	List<OfficerMasterResponse> officials = swagamService.getUsersByRg(token,
    			com.mcd.plantation.enums.UserTypeCode.UT_EMP.name(),
    			role == null ? null : role.name(), null, null, null);
        
        return officials.stream()
            .map(o -> this.toOfficialDetail(o, role, o.userCode()))
            .filter(d -> active == null || d.isActive() == active.booleanValue())
            .collect(Collectors.toList());
    }

    
    @Transactional(readOnly = true)
    public OfficialDetailResponse getOfficial(String token, String id, OfficialRole role) {
    	
    	List<OfficerMasterResponse> officials = swagamService.getUsersByRg(token,
    			com.mcd.plantation.enums.UserTypeCode.UT_EMP.name(),
    			role == null ? null : role.name(), null, null, null);
    	
    	OfficerMasterResponse found = swagamService.findFirstByCode(officials, id).orElse(null);
    	if (found == null) {
    		// Not in the (possibly offline) directory — try the local table
    		// by official id or employee id before giving up.
    		McdOfficial local = localOfficial(id);
    		if (local == null) {
    			throw ResourceNotFoundException.of("Official", id);
    		}
    		found = new OfficerMasterResponse(local.getEmployeeId(), local.getName(), null, null, null);
    	}
        return toOfficialDetail(found, role, id);
    }

    // ── Citizens ──────────────────────────────────────────────────

    /**
     * Citizens are held in the local {@code citizens} table — no external
     * service involved.
     *
     * @param active {@code null} = all, otherwise only active/inactive rows
     */
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getAllCitizens(Boolean active) {
        List<Citizen> citizens = citizenRepo.findAllByOrderByFullName();
        if (active != null) {
            citizens = citizens.stream()
                    .filter(c -> c.isActive() == active.booleanValue())
                    .collect(Collectors.toList());
        }
        return citizens.stream()
                .map(c -> new UserSummaryResponse(
                        c.getCitizenId().toString(),
                        c.getFullName(),
                        c.getEmail(),
                        c.getPhone(),
                        c.getAadhaarLast4(),
                        c.getAddress(),
                        c.isActive(),
                        c.getRegisteredAt(),
                        c.getLastLoginAt()))
                .collect(Collectors.toList());
    }

	/*
	 * @Transactional public OfficialDetailResponse
	 * createOfficial(CreateOfficialRequest req) { if
	 * (officialRepo.existsByEmail(req.email())) throw new
	 * DuplicateResourceException("Email already registered: " + req.email()); if
	 * (officialRepo.existsByEmployeeId(req.employeeId())) throw new
	 * DuplicateResourceException("Employee ID already in use: " +
	 * req.employeeId());
	 * 
	 * Zone zone = null; if (req.zoneId() != null) zone =
	 * zoneRepo.findById(req.zoneId()) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Zone", req.zoneId()));
	 * 
	 * if (req.role() != OfficialRole.ADMIN && zone == null) throw new
	 * IllegalArgumentException( "Zone is mandatory for " + req.role() +
	 * " officials.");
	 * 
	 * Ward ward = null; if (req.wardId() != null) { ward =
	 * wardRepo.findById(req.wardId()) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Ward", req.wardId())); if (zone != null &&
	 * !ward.getZone().getZoneId().equals(zone.getZoneId())) throw new
	 * IllegalArgumentException( "Ward '" + ward.getName() +
	 * "' does not belong to the specified zone."); }
	 * 
	 * McdOfficial official = McdOfficial.builder() .name(req.name())
	 * .employeeId(req.employeeId()) .email(req.email()) .phone(req.phone())
	 * .role(req.role()) .zone(zone) .ward(ward) .isActive(true)
	 * .passwordHash(passwordEncoder.encode(req.password())) .build(); return
	 * toOfficialDetail(officialRepo.save(official)); }
	 */

	/*
	 * @Transactional public OfficialDetailResponse updateOfficial(String id,
	 * UpdateOfficialRequest req) { McdOfficial official = officialRepo.findById(id)
	 * .orElseThrow(() -> ResourceNotFoundException.of("Official", id));
	 * 
	 * if (req.employeeId() != null &&
	 * !req.employeeId().equals(official.getEmployeeId()) &&
	 * officialRepo.existsByEmployeeIdAndOfficialIdNot(req.employeeId(), id)) throw
	 * new DuplicateResourceException("Employee ID already in use: " +
	 * req.employeeId());
	 * 
	 * official.setName(req.name()); if (req.employeeId() != null)
	 * official.setEmployeeId(req.employeeId()); //if (req.phone() != null)
	 * official.setPhone(req.phone()); return
	 * toOfficialDetail(officialRepo.save(official)); }
	 */

	/*
	 * @Transactional public OfficialDetailResponse changeRole(String id,
	 * OfficialRole newRole) { McdOfficial official = officialRepo.findById(id)
	 * .orElseThrow(() -> ResourceNotFoundException.of("Official", id)); if (newRole
	 * != OfficialRole.R_HORTIC_ADM && official.getZone() == null) throw new
	 * IllegalArgumentException(
	 * "Assign a zone to this official before changing role to " + newRole + ".");
	 * official.setRole(newRole); return
	 * toOfficialDetail(officialRepo.save(official)); }
	 */

    
	/*
	 * @Transactional public OfficialDetailResponse setOfficialActive(String id,
	 * boolean active) { McdOfficial official = officialRepo.findById(id)
	 * .orElseThrow(() -> ResourceNotFoundException.of("Official", id));
	 * official.setActive(active); return
	 * toOfficialDetail(officialRepo.save(official)); }
	 */

    
	/*
	 * @Transactional public void resetOfficialPassword(UUID id, String newPassword)
	 * { McdOfficial official = officialRepo.findById(id) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Official", id));
	 * official.setPasswordHash(passwordEncoder.encode(newPassword));
	 * officialRepo.save(official); }
	 */

    // ── Citizens ──────────────────────────────────────────────────

	/*
	 * @Transactional(readOnly = true) public List<CitizenSummaryResponse>
	 * getAllCitizens(Boolean active) { return
	 * citizenRepo.findAllByOrderByFullName().stream() .filter(c -> active == null
	 * || c.isActive() == active) .map(this::toCitizenSummary)
	 * .collect(Collectors.toList()); }
	 */

    public UserSummaryResponse getCitizen(String id, String userTypeCode) {
        return toCitizenSummary(id, userTypeCode);
    }

	/*
	 * @Transactional public CitizenSummaryResponse setCitizenActive(UUID id,
	 * boolean active) { Citizen citizen = citizenRepo.findById(id) .orElseThrow(()
	 * -> ResourceNotFoundException.of("Citizen", id)); citizen.setActive(active);
	 * return toCitizenSummary(citizenRepo.save(citizen)); }
	 */

	/*
	 * @Transactional public void resetCitizenPassword(UUID id, String newPassword)
	 * { Citizen citizen = citizenRepo.findById(id) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Citizen", id));
	 * citizen.setPasswordHash(passwordEncoder.encode(newPassword));
	 * citizenRepo.save(citizen); }
	 */

    // ── Mappers ───────────────────────────────────────────────────

    /**
     * @param requestedRole role from the query string (may be {@code null})
     * @param lookupCode    code used to enrich the row from the local
     *                      {@code mcd_officials} table (e-mail, phone,
     *                      active flag, zone and — authoritative — the role)
     */
    private OfficialDetailResponse toOfficialDetail(OfficerMasterResponse o, OfficialRole requestedRole, String lookupCode) {

    	McdOfficial local = localOfficial(lookupCode != null ? lookupCode : o.userCode());

    	OfficialRole role = requestedRole;
    	if (local != null && local.getRole() != null) {
    		role = local.getRole();
    	} else if (role == null) {
    		role = OfficialRole.R_HORTIC_OFF;
    	}

    	ZoneResponse zone;
    	if (local != null && local.getZoneId() != null) {
    		String zoneId = local.getZoneId().toString();
    		zone = new ZoneResponse(zoneId, masterDataCache.getZoneName(zoneId), zoneId, true);
    	} else {
    		zone = new ZoneResponse(
    				masterDataCache.getZoneCodeForWrapperCode(o.wrapperOrgCode()), 
    				masterDataCache.getZoneNameForWrapperCode(o.wrapperOrgCode()), 
    				masterDataCache.getZoneCodeForWrapperCode(o.wrapperOrgCode()), 
    				true);
    	}

        return new OfficialDetailResponse(
            o.userCode(),
            o.userName() != null ? o.userName() : (local != null ? local.getName() : null),
            o.userCode(),
            local != null ? local.getEmail() : "",
            local != null ? local.getPhone() : "",
            role,
            zone,
            local != null ? local.isActive() : true,
            local != null ? local.getCreatedAt() : null);
    }

    /**
     * Local credentials row for an official, matched on official id first and
     * on employee id second (both are used as identifiers across the app).
     */
    private McdOfficial localOfficial(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String trimmed = code.trim();
        return officialRepo.findById(trimmed)
                .or(() -> officialRepo.findByEmployeeId(trimmed).stream().findFirst())
                .orElse(null);
    }

    private UserSummaryResponse toCitizenSummary(String citizenId, String userTypeCode) {
        return swagamService.fetchUserSummary(citizenId, userTypeCode);
    }
}
