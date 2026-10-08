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
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


// ════════════════════════════════════════════════════════════════
//  MCD MASTER INVENTORY CONTROLLER  — /mcd/master-inventory
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/master-inventory") @RequiredArgsConstructor
@Tag(name = "MCD — Master Inventory")
@SecurityRequirement(name = "bearerAuth")
//??@PreAuthorize("hasRole('ADMIN')")
public class McdMasterInventoryController {

	@Value("${rolecode.plantation.admin}")    private String adminRoleCode;
	
	
    private final MasterInventoryService masterInventoryService;

    @GetMapping
    @Operation(summary = "List master tree inventory — total, allocated across all slots, available")
    public ResponseEntity<ApiResponse<List<MasterInventoryResponse>>> getAll(
    		Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode))
    	{

    		return ResponseEntity.ok(ApiResponse.ok(masterInventoryService.getAll()));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access");
    	}
        
    }

    @PutMapping
    @Operation(summary = "Set total qty for a species in master inventory (create or update)")
    public ResponseEntity<ApiResponse<MasterInventoryResponse>> upsert(
    		Authentication authentication,
            @Valid @RequestBody SetMasterStockRequest req
            //@AuthenticationPrincipal McdOfficial official
            ) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode))
    	{

    		String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;

    		return ResponseEntity.ok(ApiResponse.ok("Master inventory updated",
    	            masterInventoryService.upsert(req, loginUserSystemCode)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access");
    	}
    		
        
    }

    @GetMapping("/history")
    @Operation(summary = "Audit log — all master stock changes and park allocations, newest first")
    public ResponseEntity<ApiResponse<List<MasterInventoryHistoryResponse>>> getHistory(
    		Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode))
    	{

    		String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;

    		return ResponseEntity.ok(ApiResponse.ok(masterInventoryService.getHistory()));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access");
    	}
    }
}
