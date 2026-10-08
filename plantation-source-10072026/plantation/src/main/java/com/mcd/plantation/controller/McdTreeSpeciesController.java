package com.mcd.plantation.controller;

import com.mcd.plantation.dto.request.*;
import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.exception.UnauthorizedException;
import com.mcd.plantation.pojo.UserToken;

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
//  MCD TREE SPECIES CONTROLLER  — /mcd/trees
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/trees") @RequiredArgsConstructor
@Tag(name = "MCD — Tree Species, Admin Only Access")
@SecurityRequirement(name = "bearerAuth")
//??@PreAuthorize("hasRole('ADMIN')")
public class McdTreeSpeciesController {

    private final com.mcd.plantation.service.impl.TreeSpeciesService treeSpeciesService;

    @Value("${rolecode.plantation.admin}")    
    private String adminRoleCode;
    
    @GetMapping
    @Operation(summary = "List all tree species including inactive")
    public ResponseEntity<ApiResponse<List<com.mcd.plantation.dto.response.TreeSpeciesResponse>>> getAll(
    		Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(treeSpeciesService.getAll()));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
    }

    @GetMapping("/{speciesId}")
    @Operation(summary = "Get a single tree species by ID")
    public ResponseEntity<ApiResponse<com.mcd.plantation.dto.response.TreeSpeciesResponse>> getOne(
            @PathVariable UUID speciesId,
            Authentication authentication) {
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(treeSpeciesService.getOne(speciesId)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
        
    }

    @PostMapping
    @Operation(summary = "Create a new tree species")
    public ResponseEntity<ApiResponse<com.mcd.plantation.dto.response.TreeSpeciesResponse>> create(
            @Valid @RequestBody CreateTreeSpeciesRequest req, Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.status(HttpStatus.CREATED)
    	            .body(ApiResponse.ok("Tree species created", treeSpeciesService.create(req)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
        
    }

    @PutMapping("/{speciesId}")
    @Operation(summary = "Update an existing tree species")
    public ResponseEntity<ApiResponse<com.mcd.plantation.dto.response.TreeSpeciesResponse>> update(
            @PathVariable UUID speciesId,
            @Valid @RequestBody CreateTreeSpeciesRequest req,
            Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok("Tree species updated", treeSpeciesService.update(speciesId, req)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
        
    }

    @DeleteMapping("/{speciesId}")
    @Operation(summary = "Deactivate a tree species (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID speciesId,
    		Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode))
    	{
    		treeSpeciesService.deactivate(speciesId);
            return ResponseEntity.ok(ApiResponse.ok("Tree species deactivated", null));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
        
    }

    @PutMapping("/{speciesId}/occasions")
    @Operation(summary = "Replace the preferred occasions for a tree species")
    public ResponseEntity<ApiResponse<com.mcd.plantation.dto.response.TreeSpeciesResponse>> updateOccasions(
            @PathVariable UUID speciesId,
            @RequestBody List<UUID> occasionIds,
            Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok("Occasions updated",
    	            treeSpeciesService.updateOccasions(speciesId, occasionIds)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
        
    }
}