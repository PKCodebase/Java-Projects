package com.mcd.plantation.controller;

import com.mcd.plantation.dto.response.ApiResponse;
import com.mcd.plantation.dto.response.UserSummaryResponse;
import com.mcd.plantation.exception.UnauthorizedException;
import com.mcd.plantation.pojo.UserToken;
import com.mcd.plantation.service.impl.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ════════════════════════════════════════════════════════════════
//  MCD CITIZENS CONTROLLER  — /mcd/citizens   (Admin only)
//
//  Served from the local citizens table; no external service involved.
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/citizens") @RequiredArgsConstructor
@Tag(name = "MCD — Citizens (Admin Only)")
@SecurityRequirement(name = "bearerAuth")
public class McdCitizensController {

    private final UserManagementService userMgmtService;

    @Value("${rolecode.plantation.admin}")
    private String adminRoleCode;

    @GetMapping
    @Operation(summary = "List registered citizens — filter by active status")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getAllCitizens(
            Authentication authentication,
            @RequestParam(required = false) Boolean active) {

        String roleCode = ((UserToken) authentication.getPrincipal()).getRoleCode();
        if (!adminRoleCode.equals(roleCode)) {
            throw new UnauthorizedException("Unauthorized Access, Only Admin can View");
        }
        return ResponseEntity.ok(ApiResponse.ok(userMgmtService.getAllCitizens(active)));
    }
}
