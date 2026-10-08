package com.mcd.plantation.controller;


import com.mcd.plantation.dto.request.*;
import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.exception.UnauthorizedException;
import com.mcd.plantation.pojo.UserToken;
import com.mcd.plantation.service.impl.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


// ════════════════════════════════════════════════════════════════
//  MCD ASSIGNMENT CONTROLLER  — /mcd/assignments
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/assignments") @RequiredArgsConstructor
@Tag(name = "MCD — Park Assignments")
@SecurityRequirement(name = "bearerAuth")
public class McdAssignmentController {

    private final ParkService parkService;
    
    @Value("${rolecode.plantation.admin}")    
    private String adminRoleCode;
    
    @GetMapping
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all park-official assignments (Admin Only)")
    public ResponseEntity<ApiResponse<List<ParkAssignmentResponse>>> getAllAssignments(Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(parkService.getAllAssignments()));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
    }

    @GetMapping("/parks/{parkId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all officials assigned to a specific park (Admin Only)")
    public ResponseEntity<ApiResponse<List<ParkAssignmentResponse>>> getAssignmentsForPark(
    		Authentication authentication,
            @PathVariable UUID parkId) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(parkService.getAssignmentsForPark(parkId)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
        
    }

    @GetMapping("/officials/{officialId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all parks assigned to a specific official (Admin Only)")
    public ResponseEntity<ApiResponse<List<ParkAssignmentResponse>>> getAssignmentsForOfficial(
    		Authentication authentication,
            @PathVariable String officialId) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(parkService.getAssignmentsForOfficial(officialId)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
        
    }

    @GetMapping("/officials")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all active officials (for assignment dropdown, Admin Only)")
    public ResponseEntity<ApiResponse<List<OfficialSummaryResponse>>> getAllOfficials(
    		Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(parkService.
    				getAllOfficials(authentication.getCredentials().toString())));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
    }

    
    @PostMapping("/parks/{parkId}/officials")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign an official to a park (Admin Only)")
    public ResponseEntity<ApiResponse<ParkAssignmentResponse>> assignOfficial(
    		Authentication authentication,
            @PathVariable UUID parkId,
            @Valid @RequestBody AssignOfficialRequest req
            //@AuthenticationPrincipal McdOfficial admin
            ) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode))
    	{
    		String adminUserSystemCode = authentication != null ? 
    				(authentication.getPrincipal() != null ? 
    						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
    				: null;
        	
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Official assigned",
                    parkService.assignOfficial(parkId, req.officialId(), adminUserSystemCode)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can access");
    	}
    	
    	
    }

    @DeleteMapping("/parks/{parkId}/officials/{officialId}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove an official from a park")
    public ResponseEntity<ApiResponse<Void>> removeAssignment(
    		Authentication authentication,
            @PathVariable UUID parkId,
            @PathVariable String officialId) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode))
    	{
    		parkService.removeAssignment(parkId, officialId);
            return ResponseEntity.ok(ApiResponse.ok("Assignment removed", null));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
    }
}