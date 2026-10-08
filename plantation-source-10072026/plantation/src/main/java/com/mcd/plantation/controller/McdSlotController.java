package com.mcd.plantation.controller;

import com.mcd.plantation.dto.request.*;
import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.entity.ParkSlot;
import com.mcd.plantation.pojo.UserToken;
import com.mcd.plantation.repository.ParkSlotRepository;
import com.mcd.plantation.service.impl.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

// ════════════════════════════════════════════════════════════════
//  MCD SLOT CONTROLLER  — /mcd/slots
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/slots") @RequiredArgsConstructor
@Tag(name = "MCD — Slot Management")
@SecurityRequirement(name = "bearerAuth")
//??@PreAuthorize("hasAnyRole('HORTICULTURE_OFFICER','SUPERVISOR','ADMIN')")
public class McdSlotController {

    private final ParkSlotRepository slotRepo;
    private final ParkService parkService;

    
    @PostMapping
    @Operation(summary = "Create a new time slot for a park")
    public ResponseEntity<ApiResponse<ParkSlotResponse>> createSlot(
    		Authentication authentication,
            @Valid @RequestBody CreateSlotRequest req) {

    	
    	String loginOfficerSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
        parkService.assertOfficialAssigned(req.parkId(), loginOfficerSystemCode,
        		roleCode, authentication.getCredentials().toString());

        if (!req.startTime().isBefore(req.endTime()))
            throw new IllegalArgumentException("Start time must be before end time");

        UUID noExclude = new UUID(0, 0);
        if (slotRepo.countOverlapping(req.parkId(), req.slotDate(), req.startTime(), req.endTime(), noExclude) > 0)
            throw new IllegalArgumentException(
                "A slot already exists that overlaps with " + req.startTime() + "–" + req.endTime()
                + " on " + req.slotDate() + " for this park");

        var slot = com.mcd.plantation.entity.ParkSlot.builder()
            .park(com.mcd.plantation.entity.Park.builder()
                .parkId(req.parkId()).build())
            .slotDate(req.slotDate())
            .startTime(req.startTime())
            .endTime(req.endTime())
            .capacity(req.capacity())
            .createdBy(loginOfficerSystemCode)
            .build();
        slotRepo.save(slot);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Slot created", parkService.getSlotDetail(slot.getSlotId())));
    }

    @PutMapping("/{slotId}")
    @Operation(summary = "Update a slot's date, time window, or capacity")
    public ResponseEntity<ApiResponse<ParkSlotResponse>> updateSlot(
    		Authentication authentication,
            @PathVariable UUID slotId,
            @Valid @RequestBody UpdateSlotRequest req) {

    	String loginAdminSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
        ParkSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> com.mcd.plantation.exception.ResourceNotFoundException.of("Slot", slotId));
        parkService.assertOfficialAssigned(slot.getPark().getParkId(), loginAdminSystemCode,
        		roleCode, authentication.getCredentials().toString());

        if (!req.startTime().isBefore(req.endTime()))
            throw new IllegalArgumentException("Start time must be before end time");

        if (req.capacity() < slot.getBookedCount())
            throw new IllegalArgumentException(
                "Cannot reduce capacity below booked count (" + slot.getBookedCount() + ")");

        if (slotRepo.countOverlapping(slot.getPark().getParkId(), req.slotDate(), req.startTime(), req.endTime(), slotId) > 0)
            throw new IllegalArgumentException(
                "Another slot already exists that overlaps with " + req.startTime() + "–" + req.endTime()
                + " on " + req.slotDate() + " for this park");

        slot.setSlotDate(req.slotDate());
        slot.setStartTime(req.startTime());
        slot.setEndTime(req.endTime());
        slot.setCapacity(req.capacity());
        slotRepo.save(slot);
        return ResponseEntity.ok(ApiResponse.ok("Slot updated", parkService.getSlotDetail(slotId)));
    }

    @PutMapping("/{slotId}/close")
    @Operation(summary = "Close a slot so no new bookings can be made")
    public ResponseEntity<ApiResponse<Void>> closeSlot(@PathVariable UUID slotId) {
        com.mcd.plantation.entity.ParkSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> com.mcd.plantation.exception.ResourceNotFoundException.of("Slot", slotId));
        slot.setStatus(com.mcd.plantation.enums.SlotStatus.CLOSED);
        slotRepo.save(slot);
        return ResponseEntity.ok(ApiResponse.ok("Slot closed", null));
    }
}
