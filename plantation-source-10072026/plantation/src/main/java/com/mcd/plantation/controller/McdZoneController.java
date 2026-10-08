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
//  MCD ZONE/WARD ADMIN CONTROLLER  — /mcd/zones  /mcd/wards
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/zones") @RequiredArgsConstructor
@Tag(name = "MCD — Zone, Only ADMIN access")
@SecurityRequirement(name = "bearerAuth")
//??@PreAuthorize("hasRole('ADMIN')")
public class McdZoneController {

    private final com.mcd.plantation.service.impl.ZoneWardService zoneWardService;
    
    @Value("${rolecode.plantation.admin}")    
    private String adminRoleCode;

    @GetMapping
    @Operation(summary = "List all zones")
    public ResponseEntity<ApiResponse<List<ZoneResponse>>> getAllZones() {
        return ResponseEntity.ok(ApiResponse.ok(zoneWardService.getAllZones()));
    }

	/*
	 * @PostMapping
	 * 
	 * @Operation(summary = "Create a new zone") public
	 * ResponseEntity<ApiResponse<ZoneResponse>> createZone(
	 * 
	 * @Valid @RequestBody CreateZoneRequest req) { return
	 * ResponseEntity.status(HttpStatus.CREATED)
	 * .body(ApiResponse.ok("Zone created", zoneWardService.createZone(req))); }
	 */

	/*
	 * @PutMapping("/{zoneId}")
	 * 
	 * @Operation(summary = "Update a zone") public
	 * ResponseEntity<ApiResponse<ZoneResponse>> updateZone(
	 * 
	 * @PathVariable UUID zoneId,
	 * 
	 * @Valid @RequestBody CreateZoneRequest req) { return
	 * ResponseEntity.ok(ApiResponse.ok("Zone updated",
	 * zoneWardService.updateZone(zoneId, req))); }
	 */

    
	/*
	 * @DeleteMapping("/{zoneId}")
	 * 
	 * @Operation(summary = "Deactivate a zone") public
	 * ResponseEntity<ApiResponse<Void>> deactivateZone(@PathVariable UUID zoneId) {
	 * zoneWardService.deactivateZone(zoneId); return
	 * ResponseEntity.ok(ApiResponse.ok("Zone deactivated", null)); }
	 * 
	 * @GetMapping("/{zoneId}/wards")
	 * 
	 * @Operation(summary = "List all wards for a zone including inactive") public
	 * ResponseEntity<ApiResponse<List<WardResponse>>> getAllWards(@PathVariable
	 * UUID zoneId) { return
	 * ResponseEntity.ok(ApiResponse.ok(zoneWardService.getAllWardsForZone(zoneId)))
	 * ; }
	 */

    @GetMapping("/{zoneId}/officials")
    @Operation(summary = "List active officials posted in a zone")
    public ResponseEntity<ApiResponse<List<OfficialSummaryResponse>>> getOfficialsByZone(
            @PathVariable String zoneId,
            Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(zoneWardService.getOfficialsByZone(authentication.getCredentials().toString(),
    				zoneId)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
    	
    	
        
    }
}