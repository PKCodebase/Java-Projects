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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.mcd.plantation.dto.request.UpdateOfficialZoneRequest;
import com.mcd.plantation.dto.response.ApiResponse;
import com.mcd.plantation.dto.response.OfficialDetailResponse;
import com.mcd.plantation.dto.response.OfficialSummaryResponse;
import com.mcd.plantation.enums.OfficialRole;
import com.mcd.plantation.exception.UnauthorizedException;
import com.mcd.plantation.pojo.UserToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


// ════════════════════════════════════════════════════════════════
//  MCD OFFICIALS CONTROLLER  — /mcd/officials
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/officials") @RequiredArgsConstructor
@Tag(name = "MCD — User Management: Officials (Admin Only)")
@SecurityRequirement(name = "bearerAuth")
// ?? @PreAuthorize("hasRole('ADMIN')")
public class McdOfficialsController {

    private final com.mcd.plantation.service.impl.ZoneWardService zoneWardService;
    private final com.mcd.plantation.service.impl.UserManagementService userMgmtService;
    
    @Value("${rolecode.plantation.admin}")    
    private String adminRoleCode;

    @GetMapping
    @Operation(summary = "List all officials — optionally filter by role and/or active status")
    public ResponseEntity<ApiResponse<List<com.mcd.plantation.dto.response.OfficialDetailResponse>>> getAllOfficials(
    		Authentication authentication,
            @RequestParam(required = false) com.mcd.plantation.enums.OfficialRole role,
            @RequestParam(required = false) Boolean active) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(userMgmtService.getAllOfficials(authentication.getCredentials().toString(),
    				role, active)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
    }

    @GetMapping("/{officialId}")
    @Operation(summary = "Get a single official by ID")
    public ResponseEntity<ApiResponse<com.mcd.plantation.dto.response.OfficialDetailResponse>> getOfficial(
    		Authentication authentication,
            @PathVariable String officialId) {
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(userMgmtService.getOfficial(authentication.getCredentials().toString(),
    				officialId, OfficialRole.R_HORTIC_OFF)));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
    	}
        
    }

	/*
	 * @PostMapping
	 * 
	 * @Operation(summary = "Create a new MCD official") public
	 * ResponseEntity<ApiResponse<com.mcd.plantation.dto.response.
	 * OfficialDetailResponse>> createOfficial(
	 * 
	 * @Valid @RequestBody com.mcd.plantation.dto.request.CreateOfficialRequest req)
	 * {
	 * 
	 * 
	 * return ResponseEntity.status(HttpStatus.CREATED)
	 * .body(ApiResponse.ok("Official created",
	 * userMgmtService.createOfficial(req)));
	 * 
	 * 
	 * return ResponseEntity.status(HttpStatus.CREATED)
	 * .body(ApiResponse.ok("Official created", new OfficialDetailResponse(null,
	 * null, null, null, null, null, null, null, false, null))); }
	 */

    
	/*
	 * @PutMapping("/{officialId}")
	 * 
	 * @Operation(summary = "Update official name, employeeId, and phone") public
	 * ResponseEntity<ApiResponse<com.mcd.plantation.dto.response.
	 * OfficialDetailResponse>> updateOfficial(
	 * 
	 * @PathVariable String officialId,
	 * 
	 * @Valid @RequestBody com.mcd.plantation.dto.request.UpdateOfficialRequest req)
	 * { return ResponseEntity.ok(ApiResponse.ok("Official updated",
	 * userMgmtService.updateOfficial(officialId, req))); }
	 */

    
	/*
	 * @PutMapping("/{officialId}/role")
	 * 
	 * @Operation(summary =
	 * "Change role of an official (HORTICULTURE_OFFICER / SUPERVISOR / ADMIN)")
	 * public ResponseEntity<ApiResponse<com.mcd.plantation.dto.response.
	 * OfficialDetailResponse>> changeRole(
	 * 
	 * @PathVariable String officialId,
	 * 
	 * @Valid @RequestBody com.mcd.plantation.dto.request.ChangeRoleRequest req) {
	 * return ResponseEntity.ok(ApiResponse.ok("Role updated",
	 * userMgmtService.changeRole(officialId, req.role()))); }
	 */

    
	/*
	 * @PutMapping("/{officialId}/zone")
	 * 
	 * @Operation(summary =
	 * "Assign or update zone (mandatory) and ward (optional) for an official")
	 * public ResponseEntity<ApiResponse<OfficialSummaryResponse>> assignZone(
	 * 
	 * @PathVariable String officialId,
	 * 
	 * @Valid @RequestBody UpdateOfficialZoneRequest req) { return
	 * ResponseEntity.ok(ApiResponse.ok("Zone assigned",
	 * zoneWardService.assignOfficialZone(officialId, req))); }
	 */

	/*
	 * @PutMapping("/{officialId}/activate")
	 * 
	 * @Operation(summary = "Activate an official account") public
	 * ResponseEntity<ApiResponse<com.mcd.plantation.dto.response.
	 * OfficialDetailResponse>> activateOfficial(
	 * 
	 * @PathVariable String officialId) { return
	 * ResponseEntity.ok(ApiResponse.ok("Official activated",
	 * userMgmtService.setOfficialActive(officialId, true))); }
	 */

	/*
	 * @PutMapping("/{officialId}/deactivate")
	 * 
	 * @Operation(summary = "Deactivate an official account (prevents login)")
	 * public ResponseEntity<ApiResponse<com.mcd.plantation.dto.response.
	 * OfficialDetailResponse>> deactivateOfficial(
	 * 
	 * @PathVariable String officialId) { return
	 * ResponseEntity.ok(ApiResponse.ok("Official deactivated",
	 * userMgmtService.setOfficialActive(officialId, false))); }
	 */

	/*
	 * @PutMapping("/{officialId}/password")
	 * 
	 * @Operation(summary = "Admin reset password for an official") public
	 * ResponseEntity<ApiResponse<Void>> resetOfficialPassword(
	 * 
	 * @PathVariable UUID officialId,
	 * 
	 * @Valid @RequestBody com.mcd.plantation.dto.request.AdminResetPasswordRequest
	 * req) { userMgmtService.resetOfficialPassword(officialId, req.newPassword());
	 * return ResponseEntity.ok(ApiResponse.ok("Password reset successfully",
	 * null)); }
	 */
}