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
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;

// ════════════════════════════════════════════════════════════════
//  MCD PARK CONTROLLER  — /mcd/parks
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/parks") @RequiredArgsConstructor
@Tag(name = "MCD — Park Management")
@SecurityRequirement(name = "bearerAuth")
//??@PreAuthorize("hasAnyRole('HORTICULTURE_OFFICER','SUPERVISOR','ADMIN')")
public class McdParkController {

    private final ParkService parkService;
    
    @Value("${rolecode.plantation.admin}")    
    private String adminRoleCode;
    
    @Value("${rolecode.plantation.horticultre-admin}")    
    private String horticultreAdminRoleCode;

    @GetMapping
    @Operation(summary = "List all parks including inactive (admin view)")
    public ResponseEntity<ApiResponse<List<ParkResponse>>> getAllParks(
    		Authentication authentication,
            @RequestParam(required = false) String zoneId) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(parkService.getAllParksAdmin(zoneId)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
        
    }

    @GetMapping("/paged")
    @Operation(summary = "Paginated park list with zone and name filters (admin management)")
    public ResponseEntity<ApiResponse<Page<ParkResponse>>> getAllParksPaged(
    		Authentication authentication,
            @RequestParam(required = false) String zoneId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(parkService.getParksAdminPageable(zoneId, name, page, size)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
    }

    @PostMapping
    @Operation(summary = "Create a new park")
    public ResponseEntity<ApiResponse<ParkResponse>> createPark(
            @Valid @RequestBody CreateParkRequest req,
            Authentication authentication) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode) || roleCode.equals(horticultreAdminRoleCode))
    	{
    		String loginUserSystemCode = authentication != null ? 
    				(authentication.getPrincipal() != null ? 
    						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
    				: null;
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Park created", parkService.createPark(req, loginUserSystemCode,
                		roleCode, authentication.getCredentials().toString())));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin/Horticultre Admin can View");
    	}
    }

    @PutMapping("/{parkId}")
    @Operation(summary = "Update park details")
    public ResponseEntity<ApiResponse<ParkResponse>> updatePark(
    		Authentication authentication,
            @PathVariable UUID parkId,
            @Valid @RequestBody CreateParkRequest req
            //@AuthenticationPrincipal McdOfficial official
            ) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	
    	if(roleCode.equals(adminRoleCode) || roleCode.equals(horticultreAdminRoleCode))
    	{
    		String loginUserSystemCode = authentication != null ? 
    				(authentication.getPrincipal() != null ? 
    						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
    				: null;
    		return ResponseEntity.
    				ok(ApiResponse.ok("Park updated", parkService.updatePark(parkId, req, loginUserSystemCode,
    						roleCode, authentication.getCredentials().toString())));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin/Horticultre Admin can View");
    	}
    }

    @DeleteMapping("/{parkId}")
    //??@PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @Operation(summary = "Deactivate a park (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deactivatePark(@PathVariable UUID parkId) {
    	
        parkService.deactivatePark(parkId);
        return ResponseEntity.ok(ApiResponse.ok("Park deactivated", null));
    }
}