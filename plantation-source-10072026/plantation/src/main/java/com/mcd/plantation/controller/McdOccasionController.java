package com.mcd.plantation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mcd.plantation.dto.response.ApiResponse;
import com.mcd.plantation.exception.UnauthorizedException;
import com.mcd.plantation.pojo.UserToken;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


// ════════════════════════════════════════════════════════════════
//  MCD OCCASIONS ADMIN CONTROLLER  — /mcd/occasions
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/occasions") @RequiredArgsConstructor
@Tag(name = "MCD — Occasions (Admin Only)")
@SecurityRequirement(name = "bearerAuth")
// ?? @PreAuthorize("hasRole('ADMIN')")
public class McdOccasionController {

    private final com.mcd.plantation.service.impl.OccasionService occasionService;
    
    @Value("${rolecode.plantation.admin}")    
    private String adminRoleCode;

    @GetMapping
    @Operation(summary = "List all occasions including inactive")
    public ResponseEntity<ApiResponse<List<com.mcd.plantation.dto.response.OccasionResponse>>> getAll(
    		Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(occasionService.getAllAdmin()));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
    }

    @PostMapping
    @Operation(summary = "Create a new occasion")
    public ResponseEntity<ApiResponse<com.mcd.plantation.dto.response.OccasionResponse>> create(
    		Authentication authentication,
            @Valid @RequestBody com.mcd.plantation.dto.request.CreateOccasionRequest req) {
    	
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.status(HttpStatus.CREATED)
    	            .body(ApiResponse.ok("Occasion created", occasionService.create(req)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can create");
    	}
        
    }

    @PutMapping("/{occasionId}")
    @Operation(summary = "Update an existing occasion")
    public ResponseEntity<ApiResponse<com.mcd.plantation.dto.response.OccasionResponse>> update(
    		Authentication authentication,
            @PathVariable UUID occasionId,
            @Valid @RequestBody com.mcd.plantation.dto.request.CreateOccasionRequest req) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok("Occasion updated", occasionService.update(occasionId, req)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can update");
    	}
        
    }
}