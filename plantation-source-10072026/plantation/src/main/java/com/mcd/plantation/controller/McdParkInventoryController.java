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
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


// ════════════════════════════════════════════════════════════════
//  MCD PARK INVENTORY CONTROLLER  — /mcd/parks/{parkId}/inventory
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/parks/{parkId}/inventory") @RequiredArgsConstructor
@Tag(name = "MCD — Park Inventory (Admin Only)")
@SecurityRequirement(name = "bearerAuth")
//??@PreAuthorize("hasRole('ADMIN')")
public class McdParkInventoryController {

    private final MasterInventoryService masterInventoryService;
    
    @Value("${rolecode.plantation.admin}")    
    private String adminRoleCode;
    
    @Value("${rolecode.plantation.horticultre-admin}")    
    private String horticultreAdminRoleCode;

    @GetMapping
    @Operation(summary = "List tree species allocated to a park with used/available breakdown")
    public ResponseEntity<ApiResponse<List<ParkInventoryResponse>>> getParkInventory(
    		Authentication authentication,
            @PathVariable UUID parkId) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode) || roleCode.equals(horticultreAdminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(masterInventoryService.getParkInventory(parkId)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
    }

    @PostMapping
    @Operation(summary = "Allocate a tree species from master pool to a park")
    public ResponseEntity<ApiResponse<ParkInventoryResponse>> allocate(
            @PathVariable UUID parkId,
            @Valid @RequestBody AllocateToParkRequest req,
            Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode) || roleCode.equals(horticultreAdminRoleCode))
    	{
    		String loginUserSystemCode = authentication != null ? 
    				(authentication.getPrincipal() != null ? 
    						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
    				: null;
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Species allocated to park",
                    masterInventoryService.allocateToPark(parkId, req, loginUserSystemCode)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
    }

    @PutMapping("/{parkInvId}")
    @Operation(summary = "Update the allocated quantity for a park-species entry")
    public ResponseEntity<ApiResponse<ParkInventoryResponse>> update(
            @PathVariable UUID parkId,
            @PathVariable UUID parkInvId,
            @Valid @RequestBody UpdateParkInventoryRequest req,
            Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode) || roleCode.equals(horticultreAdminRoleCode))
    	{
    		String loginUserSystemCode = authentication != null ? 
    				(authentication.getPrincipal() != null ? 
    						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
    				: null;
            return ResponseEntity.ok(ApiResponse.ok("Park inventory updated",
                masterInventoryService.updateParkInventory(parkId, parkInvId, req, loginUserSystemCode)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
    }

    @DeleteMapping("/{parkInvId}")
    @Operation(summary = "Remove a species allocation from a park (only if no slot usage)")
    public ResponseEntity<ApiResponse<Void>> remove(
            @PathVariable UUID parkId,
            @PathVariable UUID parkInvId,
            Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode) || roleCode.equals(horticultreAdminRoleCode))
    	{
    		String loginUserSystemCode = authentication != null ? 
    				(authentication.getPrincipal() != null ? 
    						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
    				: null;
            masterInventoryService.removeParkInventory(parkId, parkInvId, loginUserSystemCode);
            return ResponseEntity.ok(ApiResponse.ok("Park inventory allocation removed", null));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
    }
}