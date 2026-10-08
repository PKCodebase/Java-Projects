package com.mcd.plantation.controller;

import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.pojo.UserToken;
import com.mcd.plantation.service.impl.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;


// ════════════════════════════════════════════════════════════════
//  MCD MY-PARKS CONTROLLER  — /mcd/my-parks
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/my-parks") @RequiredArgsConstructor
@Tag(name = "MCD — My Parks")
@SecurityRequirement(name = "bearerAuth")
//??@PreAuthorize("hasAnyRole('HORTICULTURE_OFFICER','SUPERVISOR','ADMIN')")
public class McdMyParksController {

    private final ParkService parkService;

    @GetMapping
    @Operation(summary = "Get parks assigned to the current official (ADMIN sees all parks)")
    public ResponseEntity<ApiResponse<List<ParkResponse>>> getMyParks(
    		Authentication authentication) {
    	String loginUserSystemCode = authentication != null ? 
				(authentication.getPrincipal() != null ? 
						((UserToken)authentication.getPrincipal()).getUserSystemCode() : null) 
				: null;
    	String roleCode = ((UserToken)authentication.getPrincipal()).getRoleCode();
        return ResponseEntity.ok(ApiResponse.ok(parkService.getParksForOfficial(loginUserSystemCode, roleCode)));
    }
    
    
}