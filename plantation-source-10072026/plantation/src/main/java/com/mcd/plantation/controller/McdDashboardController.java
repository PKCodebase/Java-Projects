package com.mcd.plantation.controller;

import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.exception.UnauthorizedException;
import com.mcd.plantation.pojo.UserToken;
import com.mcd.plantation.service.impl.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


// ════════════════════════════════════════════════════════════════
//  MCD DASHBOARD CONTROLLER  — /mcd/dashboard
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/dashboard") @RequiredArgsConstructor
@Tag(name = "MCD — Admin Dashboard")
@SecurityRequirement(name = "bearerAuth")
//@PreAuthorize("hasAnyRole('HORTICULTURE_OFFICER','SUPERVISOR','ADMIN')")
public class McdDashboardController {

    private final DashboardService dashboardService;
    
    @Value("${rolecode.plantation.admin}")    
    private String adminRoleCode;
    
    @Value("${rolecode.plantation.horticultre-admin}")    
    private String horticultreAdminRoleCode;

    @GetMapping
    @Operation(summary = "Real-time overview of the plantation drive — KPIs, trends, zone/species breakdown")
    public ResponseEntity<ApiResponse<DashboardOverview>> getOverview(
    		Authentication authentication) {
    	
    	
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
    	if(roleCode.equals(adminRoleCode) || roleCode.equals(horticultreAdminRoleCode))
    	{
    		return ResponseEntity.ok(ApiResponse.ok(dashboardService.getOverview()));
    	}
    	else
    	{
    		throw new UnauthorizedException("Unauthorized Access, Only Admin/Horticultre Admin can View");
    	}
    	
        
    }
}
