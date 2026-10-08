package com.mcd.plantation.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.mcd.plantation.dto.response.ZoneCacheResponse;

@Component
public class MasterDataCache {
	
	private final RestTemplate restTemplate = new RestTemplate();

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Value("${swagam.api-base-url}")
	private String baseUrl;

	private Map<String, String> zoneMap = new ConcurrentHashMap<>();
	private Map<String, String> wrapperZoneNameMap = new ConcurrentHashMap<>();
	private Map<String, String> wrapperZoneCodeMap = new ConcurrentHashMap<>();

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		
		loadZones();
		logCacheStatus();
	}

	public Map<String, String> getZones() {
		return zoneMap;
	}
	
	public String getZoneName(String zoneCode) {
		if (zoneCode == null || zoneCode.isBlank())
			return "";
		return zoneMap.getOrDefault(zoneCode, "");
	}
	
	public String getZoneNameForWrapperCode(String wrapperCode) {
		if (wrapperCode == null || wrapperCode.isBlank())
			return "";
		return wrapperZoneNameMap.getOrDefault(wrapperCode, "");
	}
	
	public String getZoneCodeForWrapperCode(String wrapperCode) {
		if (wrapperCode == null || wrapperCode.isBlank())
			return "";
		return wrapperZoneCodeMap.getOrDefault(wrapperCode, "");
	}
	

	public void reloadAll() {
		loadZones();
		logCacheStatus();
	}

	public void reloadZones() {
		loadZones();
		logCacheStatus();
	}

	private void loadZones() {
		
		try {
			
	        String url = baseUrl.concat("/obps/mst/cache?code=ZONESWITHWRAPPER");
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        headers.set("X-SESS-NONCE", UUID.randomUUID().toString()); 
	        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
	        ResponseEntity<List<ZoneCacheResponse>> response = restTemplate.exchange(
	                url,
	                HttpMethod.GET,
	                requestEntity,
	                new ParameterizedTypeReference<List<ZoneCacheResponse>>() {}
	        );
	        
	        Map<String, String> freshZones = response.getBody().stream()
                    .filter(zone -> zone.code() != null && zone.name() != null)
                    .collect(Collectors.toMap(
                    		ZoneCacheResponse::code,
                    		ZoneCacheResponse::name,
                            (existing, replacement) -> replacement 
                    ));
	        
	        Map<String, String> freshWrapperZones = response.getBody().stream()
					.filter(zone -> zone.value() != null && zone.name() != null)
					.collect(Collectors.toMap(
							ZoneCacheResponse::value, // wrapperCode
							ZoneCacheResponse::name,
							(existing, replacement) -> replacement
					));
	        
	        Map<String, String> freshWrapperWithZoneCodes = response.getBody().stream()
					.filter(zone -> zone.value() != null && zone.name() != null)
					.collect(Collectors.toMap(
							ZoneCacheResponse::value, // wrapperCode
							ZoneCacheResponse::code,
							(existing, replacement) -> replacement
					));
	        
	        this.zoneMap = new ConcurrentHashMap<>(freshZones);
	        this.wrapperZoneNameMap = new ConcurrentHashMap<>(freshWrapperZones);
	        this.wrapperZoneCodeMap = new ConcurrentHashMap<>(freshWrapperWithZoneCodes);
	        
	        
		}
		catch (Exception e) {
			// Master-data service unreachable — seed the zone cache from the
			// local database instead of leaving it empty.
			loadZonesFromDatabase();
		}
		
	}

	/**
	 * Offline fallback for {@link #loadZones()}: reads {@code zones} from the
	 * local database. The map key is the zone UUID (the same value
	 * {@code ParkService} compares a zone against), the value is the display
	 * name. Wrapper/organisation codes only exist in the external system, so
	 * those two maps stay empty while it is unreachable.
	 */
	private void loadZonesFromDatabase() {
		try {
			Map<String, String> localZones = jdbcTemplate.query(
					"select zone_id::text as code, name from zones where is_active = true order by name",
					rs -> {
						Map<String, String> found = new HashMap<>();
						while (rs.next()) {
							found.put(rs.getString("code"), rs.getString("name"));
						}
						return found;
					});
			this.zoneMap = new ConcurrentHashMap<>(localZones);
			System.out.println("Zone cache seeded from local database: " + zoneMap.size() + " zone(s)");
		} catch (Exception ex) {
			this.zoneMap = new ConcurrentHashMap<>();
			System.out.println("Zone cache is EMPTY");
		}
	}

	private void logCacheStatus() {
		System.out.println("──────────────────────────────────────────");
		System.out.println("MasterDataCache Status:");
		System.out.println("  Zones : " + zoneMap.size() + " entries");
		if (zoneMap.isEmpty())
			System.out.println("Zone cache is EMPTY");
		System.out.println("──────────────────────────────────────────");
	}
}