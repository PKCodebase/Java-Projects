package com.mcd.plantation.service.impl;

import com.mcd.plantation.config.MasterDataCache;
import com.mcd.plantation.dto.request.CreateParkRequest;
import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.entity.*;
import com.mcd.plantation.enums.OfficialRole;
import com.mcd.plantation.enums.UserTypeCode;
import com.mcd.plantation.exception.DuplicateResourceException;
import com.mcd.plantation.exception.ResourceNotFoundException;
import com.mcd.plantation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ParkService {

    private final ParkRepository parkRepo;
    private final ParkSlotRepository slotRepo;
    private final SlotTreeInventoryRepository inventoryRepo;
    private final ParkOfficialAssignmentRepository assignmentRepo;
    private final MasterDataCache masterDataCache;
    private final SwagamService swagamService;
    
    //private final McdOfficialRepository officialRepo;
    
    //private final ZoneRepository zoneRepo;
    //private final WardRepository wardRepo;
    private final ZoneWardService zoneWardService;

    @Value("${mcd.inventory.low-stock-threshold}") private int lowStockThreshold;

    // ── Public park listing (citizens) ───────────────────────────
    @Transactional(readOnly = true)
    public List<ParkResponse> getAllParks(String zoneId) {
        List<Park> parks = zoneId != null
            ? parkRepo.findByZoneAndIsActiveTrueOrderByName(zoneId)
            : parkRepo.findByIsActiveTrueOrderByName();
        return parks.stream().map(this::toParkResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ParkResponse> getParksPageable(String zoneId, int page, int size) {
        int safeSize = Math.max(1, Math.min(size, 50));
        PageRequest pageable = PageRequest.of(Math.max(0, page), safeSize, Sort.by("name"));
        Page<Park> parks = zoneId != null
            ? parkRepo.findByZoneAndIsActiveTrueOrderByName(zoneId, pageable)
            : parkRepo.findByIsActiveTrueOrderByName(pageable);
        return parks.map(this::toParkResponse);
    }

    @Transactional(readOnly = true)
    public ParkResponse getPark(UUID parkId) {
        return toParkResponse(parkRepo.findById(parkId)
            .orElseThrow(() -> ResourceNotFoundException.of("Park", parkId)));
    }

    @Transactional(readOnly = true)
    public List<ParkSlotResponse> getSlotsForPark(UUID parkId, LocalDate date) {
        return slotRepo.findByParkAndDate(parkId, date).stream()
            .map(this::toSlotResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ParkSlotResponse getSlotDetail(UUID slotId) {
        ParkSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> ResourceNotFoundException.of("Slot", slotId));
        return toSlotResponse(slot);
    }

    // ── Admin park management ─────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ParkResponse> getAllParksAdmin(String zoneId) {
        List<Park> parks;
        
		/*
		 * if (zoneId != null && wardId != null) parks =
		 * parkRepo.findByZone_ZoneIdAndWard_WardIdOrderByName(zoneId, wardId); else
		 */ 
        if (zoneId != null)
            parks = parkRepo.findByZoneOrderByName(zoneId);
        else
            parks = parkRepo.findAllByOrderByName();
        return parks.stream().map(this::toParkResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ParkResponse> getParksAdminPageable(String zoneId, String name, int page, int size) {
        int safeSize = Math.max(1, Math.min(size, 50));
        PageRequest pageable = PageRequest.of(Math.max(0, page), safeSize, Sort.by("name"));
        boolean hasZone = zoneId != null;
        boolean hasName = name != null && !name.isBlank();
        Page<Park> parks;
        if (hasZone && hasName)
            parks = parkRepo.findByZoneAndNameContainingIgnoreCaseOrderByName(zoneId, name.trim(), pageable);
        else if (hasZone)
            parks = parkRepo.findByZoneOrderByName(zoneId, pageable);
        else if (hasName)
            parks = parkRepo.findByNameContainingIgnoreCaseOrderByName(name.trim(), pageable);
        else
            parks = parkRepo.findAllByOrderByName(pageable);
        return parks.map(this::toParkResponse);
    }

    @Transactional
    public ParkResponse createPark(CreateParkRequest req, String loginOfficerUserCode, 
    		String loginOfficerRoleCode, String authToken) {
    	
    	
		/*
		 * Zone zone = zoneRepo.findById(req.zoneId()) .orElseThrow(() ->
		 * ResourceNotFoundException.of("Zone", req.zoneId()));
		 */
        
        //??
		/*
		 * McdOfficial managed =
		 * officialRepo.findByEmployeeId(loginOfficerUserCode).stream().findFirst()
		 * .orElseThrow(() -> ResourceNotFoundException.of("Official",
		 * loginOfficerUserCode));
		 */
        String loginOfficerZoneCode = null;
        if(loginOfficerRoleCode.equals(OfficialRole.R_HORTIC_OFF.name()))
        {
        	loginOfficerZoneCode = swagamService.getOfficialZoneCode(authToken, 
        			UserTypeCode.UT_EMP.name(), loginOfficerRoleCode, null, null, null);
        	if(loginOfficerZoneCode == null)
        	{
        		throw new ResourceNotFoundException("Zone is not assigned/Unable to fetch zone from service");
        	}
        }
        assertZoneAccess(loginOfficerRoleCode, loginOfficerZoneCode, req.zoneId());

        //Ward ward = resolveWard(req.wardId(), zone);

        Park park = Park.builder()
            .name(req.name()).zone(req.zoneId())
            .address(req.address())
            .city(req.city() != null ? req.city() : "Delhi")
            .latitude(req.latitude()).longitude(req.longitude())
            .description(req.description()).build();
        return toParkResponse(parkRepo.save(park));
    }

    @Transactional
    public ParkResponse updatePark(UUID parkId, CreateParkRequest req, String loginUserSystemCode,
    		String loginOfficerRoleCode, String authToken) {
        
    	Park park = parkRepo.findById(parkId)
            .orElseThrow(() -> ResourceNotFoundException.of("Park", parkId));

		/*
		 * Zone zone = zoneRepo.findById(req.zoneId()) .orElseThrow(() ->
		 * ResourceNotFoundException.of("Zone", req.zoneId()));
		 */

		/*
		 * McdOfficial managed = officialRepo.findById(loginUserSystemCode)
		 * .orElseThrow(() -> ResourceNotFoundException.of("Official", editor));
		 */
        
    	String loginOfficerZoneCode = null;
        if(loginOfficerRoleCode.equals(OfficialRole.R_HORTIC_OFF.name()))
        {
        	loginOfficerZoneCode = swagamService.getOfficialZoneCode(authToken, 
        			UserTypeCode.UT_EMP.name(), loginOfficerRoleCode, null, null, null);
        	if(loginOfficerZoneCode == null)
        	{
        		throw new ResourceNotFoundException("Zone is not assigned/Unable to fetch zone from service");
        	}
        }
        assertZoneAccess(loginOfficerRoleCode, loginOfficerZoneCode, req.zoneId());

        //Ward ward = resolveWard(req.wardId(), zone);

        park.setName(req.name());
        park.setZone(req.zoneId());
        //park.setWard(ward);
        if (req.address() != null)     park.setAddress(req.address());
        if (req.city() != null)        park.setCity(req.city());
        park.setLatitude(req.latitude());
        park.setLongitude(req.longitude());
        if (req.description() != null) park.setDescription(req.description());
        if (req.isActive() != null)    park.setActive(req.isActive());
        return toParkResponse(parkRepo.save(park));
    }

    @Transactional
    public void deactivatePark(UUID parkId) {
        Park park = parkRepo.findById(parkId)
            .orElseThrow(() -> ResourceNotFoundException.of("Park", parkId));
        park.setActive(false);
        parkRepo.save(park);
    }

    // ── Assignment-aware park listing (officials) ─────────────────
    @Transactional(readOnly = true)
    public List<ParkResponse> getParksForOfficial(String loginOfficialSystemCode,
    		String officerRoleCode) {
        
		/*
		 * McdOfficial official =
		 * officialRepo.findByEmployeeId(loginOfficialSystemCode).stream().findFirst()
		 * .orElseThrow(() -> ResourceNotFoundException.of("Official",
		 * loginOfficialSystemCode));
		 */

        if (officerRoleCode.equals(OfficialRole.R_HORTIC_ADM.name())) return getAllParksAdmin(null);

        // SUPERVISOR without zone: all parks; with zone: parks in their zone(/ward)
		/*
		 * if (officerRoleCode.equals(OfficialRole.SUPERVISOR.name())) { if
		 * (official.getZone() == null) return getAllParksAdmin(null, null); UUID zoneId
		 * = official.getZone().getZoneId(); UUID wardId = official.getWard() != null ?
		 * official.getWard().getWardId() : null; return getAllParksAdmin(zoneId,
		 * wardId); }
		 */

        // HORTICULTURE_OFFICER: explicitly assigned UNION zone/ward parks
        Set<UUID> seen = new LinkedHashSet<>();
        List<Park> result = new ArrayList<>();

        parkRepo.findByParkIdInOrderByName(
                assignmentRepo.findParkIdsByOfficialId(loginOfficialSystemCode))
            .forEach(p -> { if (seen.add(p.getParkId())) result.add(p); });

        
        
        
		/*
		 * if (official.getZone() != null) { UUID zoneId =
		 * official.getZone().getZoneId(); //UUID wardId = official.getWard() != null ?
		 * official.getWard().getWardId() : null; UUID wardId = null; List<Park>
		 * zonalParks = wardId != null ?
		 * parkRepo.findByZone_ZoneIdAndWard_WardIdOrderByName(zoneId, wardId) :
		 * parkRepo.findByZone_ZoneIdOrderByName(zoneId); zonalParks.forEach(p -> { if
		 * (seen.add(p.getParkId())) result.add(p); }); }
		 */

        result.sort(Comparator.comparing(Park::getName));
        return result.stream().map(this::toParkResponse).collect(Collectors.toList());
    }

    // ── Assignment management (admin) ─────────────────────────────
    @Transactional(readOnly = true)
    public List<ParkAssignmentResponse> getAllAssignments() {
        return assignmentRepo.findAll().stream()
            .map(this::toAssignmentResponse)
            .sorted(Comparator.comparing(ParkAssignmentResponse::parkName))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParkAssignmentResponse> getAssignmentsForPark(UUID parkId) {
        return assignmentRepo.findByPark_ParkId(parkId).stream()
            .map(this::toAssignmentResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParkAssignmentResponse> getAssignmentsForOfficial(String officialId) {
        
		/*
		 * officialRepo.findById(officialId) .orElseThrow(() ->
		 * ResourceNotFoundException.of("Official", officialId));
		 */
    	
        return assignmentRepo.findByOfficial(officialId).stream()
            .map(this::toAssignmentResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OfficialSummaryResponse> getAllOfficials(String token) {
    	
    	//????????????????????????????? ALL users API
    	
    	List<OfficerMasterResponse> officers = swagamService.getUsersByRg(token, UserTypeCode.UT_EMP.name(),
    			OfficialRole.R_HORTIC_ADM.name(), null, null, null);
    	
    	
    	
        return officers.stream()
            .map(this::toOfficialSummaryResponse).collect(Collectors.toList());
        
    }

    @Transactional
    public ParkAssignmentResponse assignOfficial(UUID parkId, String officialId, String assignedById) {
        if (assignmentRepo.existsByPark_ParkIdAndOfficial(parkId, officialId))
            throw new DuplicateResourceException("Official is already assigned to this park.");
        Park park = parkRepo.findById(parkId)
            .orElseThrow(() -> ResourceNotFoundException.of("Park", parkId));
        
		/*
		 * McdOfficial official = officialRepo.findById(officialId) .orElseThrow(() ->
		 * ResourceNotFoundException.of("Official", officialId));
		 */
        
		/* McdOfficial assignedBy = officialRepo.findById(assignedById).orElse(null); */
        
        ParkOfficialAssignment a = ParkOfficialAssignment.builder()
            .park(park).official(officialId).assignedBy(assignedById).build();
        assignmentRepo.save(a);
        return toAssignmentResponse(a);
    }

    @Transactional
    public void removeAssignment(UUID parkId, String officialId) {
        ParkOfficialAssignment a = assignmentRepo
            .findByPark_ParkIdAndOfficial(parkId, officialId)
            .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        assignmentRepo.delete(a);
    }

    // ── Access-control helpers ────────────────────────────────────

    /**
     * Enforce that non-ADMIN officials can only create/update parks in their own zone.
     */
    private void assertZoneAccess(String officialRoleCode, String offcialZoneCode, String zoneId) {
        if (officialRoleCode.equals(OfficialRole.R_HORTIC_ADM.name())) return;
        if (offcialZoneCode == null)
            throw new AccessDeniedException("You have no zone assigned. Contact admin.");
        if (!offcialZoneCode.equals(zoneId))
            throw new AccessDeniedException(
                "You can only manage parks in your zone (" + offcialZoneCode + ").");
    }

    /**
     * Resolve ward by ID, validating it belongs to the given zone.
     */
	/*
	 * private Ward resolveWard(UUID wardId, Zone zone) { if (wardId == null) return
	 * null; Ward ward = wardRepo.findById(wardId) .orElseThrow(() ->
	 * ResourceNotFoundException.of("Ward", wardId)); if
	 * (!ward.getZone().getZoneId().equals(zone.getZoneId())) throw new
	 * IllegalArgumentException( "Ward '" + ward.getName() +
	 * "' does not belong to zone '" + zone.getName() + "'."); return ward; }
	 */

    /**
     * Grant access if: explicitly assigned, or park is in official's zone (and ward if set).
     */
    public void assertOfficialAssigned(UUID parkId, String officerId, 
    		String officerRoleCode, String authToken) {
        
		
		/*
		 * McdOfficial official =
		 * officialRepo.findByEmployeeId(officerId).stream().findFirst() .orElseThrow(()
		 * -> ResourceNotFoundException.of("Official", officerId));
		 */
		 
    	
        if (officerRoleCode.equals(OfficialRole.R_HORTIC_ADM.name()) 
        		|| officerRoleCode.equals(OfficialRole.SUPERVISOR.name())) return;
        
        if (assignmentRepo.existsByPark_ParkIdAndOfficial(parkId, officerId)) return;
        

        String loginOfficerZoneCode = null;
        if(officerRoleCode.equals(OfficialRole.R_HORTIC_OFF.name()))
        {
        	loginOfficerZoneCode = swagamService.getOfficialZoneCode(authToken, 
        			UserTypeCode.UT_EMP.name(), officerRoleCode, null, null, null);
        	if(loginOfficerZoneCode == null)
        	{
        		throw new ResourceNotFoundException("Zone is not assigned/Unable to fetch zone from service");
        	}
        }
        
        if (loginOfficerZoneCode != null) {
            Park park = parkRepo.findById(parkId)
                .orElseThrow(() -> ResourceNotFoundException.of("Park", parkId));
            
            if (park.getZone() != null
                    && park.getZone().equals(loginOfficerZoneCode)) {
            	return;
            }
        }
        throw new AccessDeniedException("You are not assigned to park " + parkId);
    }

    public boolean isOfficialAssigned(UUID parkId, String officialId) {
        return assignmentRepo.existsByPark_ParkIdAndOfficial(parkId, officialId);
    }

    // ── Slot / inventory queries ──────────────────────────────────
    public List<SlotInventoryItem> getSlotInventory(UUID slotId) {
        return inventoryRepo.findBySlotWithSpecies(slotId).stream()
            .map(i -> toInventoryItem(i, lowStockThreshold))
            .collect(Collectors.toList());
    }

    // ── Mappers ───────────────────────────────────────────────────
    private ParkResponse toParkResponse(Park p) {
        int total  = p.getSlots().stream().mapToInt(ParkSlot::getCapacity).sum();
        int booked = p.getSlots().stream().mapToInt(ParkSlot::getBookedCount).sum();
        
        ZoneResponse zone = p.getZone() != null ? zoneWardService.toZoneResponse(p.getZone()) : null;
        
        //WardResponse ward = p.getWard() != null ? zoneWardService.toWardResponse(p.getWard()) : null;
        return new ParkResponse(p.getParkId(), p.getName(), zone, 
            p.getAddress(), p.getCity(), p.getLatitude(), p.getLongitude(),
            p.getDescription(), p.isActive(), total, booked, total - booked);
    }

    private ParkSlotResponse toSlotResponse(ParkSlot s) {
        List<SlotInventoryItem> inv = s.getInventory().stream()
            .map(i -> toInventoryItem(i, lowStockThreshold)).collect(Collectors.toList());
        return new ParkSlotResponse(s.getSlotId(), s.getPark().getParkId(), s.getPark().getName(),
            s.getSlotDate(), s.getStartTime(), s.getEndTime(),
            s.getCapacity(), s.getBookedCount(), s.getFreeSpots(), s.getStatus(), inv);
    }

    private ParkAssignmentResponse toAssignmentResponse(ParkOfficialAssignment a) {
        ZoneResponse zone = a.getPark().getZone() != null
            ? zoneWardService.toZoneResponse(a.getPark().getZone()) : null;
        
        /*
        return new ParkAssignmentResponse(
            a.getAssignmentId(),
            a.getPark().getParkId(), a.getPark().getName(), zone,
            a.getOfficial().getOfficialId(), a.getOfficial().getName(),
            a.getOfficial().getEmployeeId(), a.getOfficial().getRole().name(),
            a.getAssignedAt());
            */
        //????????????????
        return new ParkAssignmentResponse(
                a.getAssignmentId(),
                a.getPark().getParkId(), a.getPark().getName(), 
                new ZoneResponse(UUID.randomUUID().toString(), "name", "code", true),
                a.getOfficial(), "Officer Name",
                a.getOfficial(), "Role name",
                a.getAssignedAt());
    }

    private OfficialSummaryResponse toOfficialSummaryResponse(OfficerMasterResponse o) {
    	
    	ZoneResponse zone = new ZoneResponse(
    			masterDataCache.getZoneCodeForWrapperCode(o.wrapperOrgCode()), 
    			masterDataCache.getZoneNameForWrapperCode(o.wrapperOrgCode()), 
    			masterDataCache.getZoneCodeForWrapperCode(o.wrapperOrgCode()), 
    			true);
    	
        return new OfficialSummaryResponse(
            o.userCode(), 
            o.userName(), 
            o.wrapperOrgCode(), 
            OfficialRole.R_HORTIC_ADM, 
            zone, 
            true);
    }

    static SlotInventoryItem toInventoryItem(SlotTreeInventory i, int threshold) {
        int avail = i.getAvailableQty();
        return new SlotInventoryItem(i.getInventoryId(), i.getSpecies().getSpeciesId(),
            i.getSpecies().getCommonName(), i.getSpecies().getScientificName(),
            i.getSpecies().getEmojiCode(), i.getSpecies().getCategory(),
            i.getSpecies().getPrice(), i.getStockQty(), i.getReservedQty(), avail,
            SlotInventoryItem.stockStatus(avail, threshold));
    }
}
