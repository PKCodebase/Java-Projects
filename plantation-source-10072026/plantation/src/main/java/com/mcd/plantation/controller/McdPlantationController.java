package com.mcd.plantation.controller;

import com.mcd.plantation.dto.request.*;
import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.pojo.UserToken;
import com.mcd.plantation.service.impl.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


// ════════════════════════════════════════════════════════════════
//  MCD PLANTATION CONTROLLER  — /mcd/plantation
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/plantation") @RequiredArgsConstructor
@Tag(name = "MCD — Plantation Actions (Admin/Horticultre Officer Role)")
@SecurityRequirement(name = "bearerAuth")
//??@PreAuthorize("hasAnyRole('HORTICULTURE_OFFICER','SUPERVISOR','ADMIN')")
public class McdPlantationController {

    private final PlantationRecordService recordService;
    private final BookingService bookingService;

    @GetMapping("/pending")
    @Operation(summary = "List bookings awaiting plantation action (SCHEDULED status)")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getPending(
    		Authentication authentication,
    		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
    		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
    	
    	String loginOfficerSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
    	
		String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
        return ResponseEntity.ok(ApiResponse.ok(
            bookingService.getScheduledBookingsForOfficial(loginOfficerSystemCode, roleCode, fromDate, toDate)));
    }
    
    @GetMapping("/pendingToday")
    @Operation(summary = "List bookings awaiting plantation action for today (SCHEDULED status)")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getPendingForToday(
    		Authentication authentication) {
    	
    	String loginOfficerSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
    	
		String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
        return ResponseEntity.ok(ApiResponse.ok(
            bookingService.getTodaysScheduledBookingsForOfficial(loginOfficerSystemCode, roleCode)));
    }

    @PutMapping("/{bookingId}/reschedule")
    @Operation(summary = "Reschedule a booking to a different slot (officer only)")
    public ResponseEntity<ApiResponse<BookingResponse>> reschedule(
            @PathVariable UUID bookingId,
            @RequestBody java.util.Map<String, UUID> body,
            Authentication authentication) {
    	String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
        UUID newSlotId = body.get("newSlotId");
        if (newSlotId == null) throw new IllegalArgumentException("newSlotId is required");
        return ResponseEntity.ok(ApiResponse.ok("Booking rescheduled",
            bookingService.rescheduleBooking(bookingId, newSlotId, loginUserSystemCode, roleCode)));
    }

    @GetMapping("/completed")
    @Operation(summary = "List completed plantations for the officer's parks, optionally filtered by park")
    public ResponseEntity<ApiResponse<List<com.mcd.plantation.dto.response.CompletedPlantationResponse>>> getCompleted(
            @RequestParam(required = false) UUID parkId,
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
    		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
    	String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
        return ResponseEntity.ok(ApiResponse.ok(recordService.getCompletedPlantations(loginUserSystemCode, parkId, roleCode,
        		fromDate, toDate)));
    }

    @PatchMapping("/{bookingId}/arrive")
    @Operation(summary = "Mark citizen as arrived — transitions SCHEDULED/PAID → WAITING for physical-presence bookings")
    public ResponseEntity<ApiResponse<BookingResponse>> markArrived(
            @PathVariable UUID bookingId,
            Authentication authentication) {
    	String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
        return ResponseEntity.ok(ApiResponse.ok("Citizen marked as arrived",
            bookingService.markCitizenArrived(bookingId, loginUserSystemCode, roleCode)));
    }

    @PostMapping("/{bookingId}")
    @Operation(summary = "Record plantation — upload photo and set tree tag. Auto-generates certificate.")
    public ResponseEntity<ApiResponse<PlantationRecordResponse>> record(
    		Authentication authentication,
            @PathVariable UUID bookingId,
            @RequestPart("data") @Valid RecordPlantationRequest req,
            @RequestPart("photo") MultipartFile photo
            //@AuthenticationPrincipal McdOfficial official
            ) {
        
        
            String loginOfficerSystemCode = authentication != null ? 
    				(authentication.getPrincipal() != null ? 
    						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
    				: null;
    	
    	return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Plantation recorded. Certificate issued.",
                recordService.recordPlantation(bookingId, req, photo, loginOfficerSystemCode)));
    }
}