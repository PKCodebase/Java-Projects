package com.mcd.plantation.service.impl;

import com.mcd.plantation.config.MasterDataCache;
import com.mcd.plantation.dto.request.UpdateOfficialZoneRequest;
import com.mcd.plantation.dto.response.OfficerMasterResponse;
import com.mcd.plantation.dto.response.OfficialSummaryResponse;
import com.mcd.plantation.dto.response.ZoneResponse;
import com.mcd.plantation.enums.OfficialRole;
import com.mcd.plantation.exception.DuplicateResourceException;
import com.mcd.plantation.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ZoneWardService {

	private final MasterDataCache masterDataCache;
	private final SwagamService swagamService;
	
	// private final McdOfficialRepository officialRepo;

	// ── Zone ─────────────────────────────────────────────────────

	// List All Zones API ??
	@Transactional(readOnly = true)
	public List<ZoneResponse> getActiveZones() {
		//return zoneRepo.findByIsActiveTrueOrderByName().stream().map(this::toZoneResponse).collect(Collectors.toList());
		return masterDataCache.getZones().entrySet().stream()
                .map(entry -> new ZoneResponse(entry.getKey(), entry.getValue(), entry.getKey(), true))
                .collect(Collectors.toList());
	}

	// List All Zones API ??
	@Transactional(readOnly = true)
	public List<ZoneResponse> getAllZones() {
		//return zoneRepo.findAllByOrderByName().stream().map(this::toZoneResponse).collect(Collectors.toList());
		
		return masterDataCache.getZones().entrySet().stream()
                .map(entry -> new ZoneResponse(entry.getKey(), entry.getValue(), entry.getKey(), true))
                .collect(Collectors.toList());
	}

	/*
	 * @Transactional public ZoneResponse createZone(CreateZoneRequest req) { if
	 * (zoneRepo.existsByName(req.name())) throw new
	 * DuplicateResourceException("Zone already exists: " + req.name()); Zone zone =
	 * Zone.builder().name(req.name()).code(req.code()).build(); return
	 * toZoneResponse(zoneRepo.save(zone)); }
	 */

	/*
	 * @Transactional public ZoneResponse updateZone(UUID zoneId, CreateZoneRequest
	 * req) { Zone zone = zoneRepo.findById(zoneId).orElseThrow(() ->
	 * ResourceNotFoundException.of("Zone", zoneId)); zone.setName(req.name()); if
	 * (req.code() != null) zone.setCode(req.code()); return
	 * toZoneResponse(zoneRepo.save(zone)); }
	 */

	/*
	 * @Transactional public void deactivateZone(UUID zoneId) { Zone zone =
	 * zoneRepo.findById(zoneId).orElseThrow(() ->
	 * ResourceNotFoundException.of("Zone", zoneId)); zone.setActive(false);
	 * zoneRepo.save(zone); }
	 */

	// ── Ward ─────────────────────────────────────────────────────

	/*
	 * @Transactional(readOnly = true) public List<WardResponse>
	 * getActiveWardsForZone(UUID zoneId) { return
	 * wardRepo.findByZone_ZoneIdAndIsActiveTrueOrderByName(zoneId).stream()
	 * .map(this::toWardResponse).collect(Collectors.toList()); }
	 */

	/*
	 * @Transactional(readOnly = true) public List<WardResponse>
	 * getAllWardsForZone(UUID zoneId) { return
	 * wardRepo.findByZone_ZoneIdOrderByName(zoneId).stream()
	 * .map(this::toWardResponse).collect(Collectors.toList()); }
	 */

	/*
	 * @Transactional public WardResponse createWard(CreateWardRequest req) { Zone
	 * zone = zoneRepo.findById(req.zoneId()) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Zone", req.zoneId())); if
	 * (wardRepo.existsByZone_ZoneIdAndName(req.zoneId(), req.name())) throw new
	 * DuplicateResourceException("Ward '" + req.name() +
	 * "' already exists in this zone."); Ward ward = Ward.builder()
	 * .zone(zone).name(req.name()).wardNumber(req.wardNumber()).build(); return
	 * toWardResponse(wardRepo.save(ward)); }
	 */

	/*
	 * @Transactional public WardResponse updateWard(UUID wardId, CreateWardRequest
	 * req) { Ward ward = wardRepo.findById(wardId) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Ward", wardId)); if
	 * (!ward.getZone().getZoneId().equals(req.zoneId())) { Zone newZone =
	 * zoneRepo.findById(req.zoneId()) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Zone", req.zoneId())); ward.setZone(newZone); }
	 * ward.setName(req.name()); if (req.wardNumber() != null)
	 * ward.setWardNumber(req.wardNumber()); return
	 * toWardResponse(wardRepo.save(ward)); }
	 */

	/*
	 * @Transactional public void deactivateWard(UUID wardId) { Ward ward =
	 * wardRepo.findById(wardId) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Ward", wardId)); ward.setActive(false);
	 * wardRepo.save(ward); }
	 */

	// ── Official zone/ward assignment ─────────────────────────────

	/*
	 * @Transactional public OfficialSummaryResponse assignOfficialZone(String
	 * officialId, UpdateOfficialZoneRequest req) { McdOfficial official =
	 * officialRepo.findById(officialId) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Official", officialId)); Zone zone =
	 * zoneRepo.findById(req.zoneId()) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Zone", req.zoneId())); official.setZone(zone);
	 * 
	 * 
	 * if (req.wardId() != null) { Ward ward = wardRepo.findById(req.wardId())
	 * .orElseThrow(() -> ResourceNotFoundException.of("Ward", req.wardId()));
	 * official.setWard(ward); } else { official.setWard(null); }
	 * 
	 * 
	 * return toOfficialSummaryResponse(officialRepo.save(official)); }
	 */
	
	@Transactional(readOnly = true)
	public List<OfficialSummaryResponse> getOfficialsByZone(String token, String zoneId) {
		
		List<OfficerMasterResponse> officialsList = swagamService.getUsersByRg(token, "EMPLOYEE",
    			OfficialRole.R_HORTIC_ADM.name(), null, zoneId, null);
		return officialsList.stream()
				.map(this::toOfficialSummaryResponse).collect(Collectors.toList());
	}

	// ── Mappers ───────────────────────────────────────────────────

	public ZoneResponse toZoneResponse(String zoneId) {
		// API to get zone detail on code
		//return new ZoneResponse(zone, z.getName(), z.getCode(), z.isActive());
		return new ZoneResponse(zoneId, masterDataCache.getZoneName(zoneId), zoneId, true);
	}

	/*
	 * public WardResponse toWardResponse(Ward w) { return new
	 * WardResponse(w.getWardId(), w.getZone().getZoneId(), w.getZone().getName(),
	 * w.getName(), w.getWardNumber(), w.isActive()); }
	 */

	private OfficialSummaryResponse toOfficialSummaryResponse(OfficerMasterResponse o) {
		
		ZoneResponse zone = new ZoneResponse(
    			masterDataCache.getZoneCodeForWrapperCode(o.wrapperOrgCode()), 
    			masterDataCache.getZoneNameForWrapperCode(o.wrapperOrgCode()), 
    			masterDataCache.getZoneCodeForWrapperCode(o.wrapperOrgCode()), 
    			true);
		
		//WardResponse ward = o.getWard() != null ? toWardResponse(o.getWard()) : null;
		return new OfficialSummaryResponse(o.userCode(), o.userName(), o.userCode(), OfficialRole.R_HORTIC_ADM, zone, 
				true);
	}
}
