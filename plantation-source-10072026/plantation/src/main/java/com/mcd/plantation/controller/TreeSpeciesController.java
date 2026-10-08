package com.mcd.plantation.controller;

import com.mcd.plantation.dto.response.ApiResponse;
import com.mcd.plantation.dto.response.TreeSpeciesResponse;
import com.mcd.plantation.exception.UnauthorizedException;
import com.mcd.plantation.pojo.UserToken;
import com.mcd.plantation.service.impl.TreeSpeciesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// ════════════════════════════════════════════════════════════════
//  TREE SPECIES CONTROLLER  — /trees (public read catalog)
//
//  SecurityConfig already permits "/trees" and "/trees/**", so these two
//  endpoints are the only species reads reachable without a token. They back
//  the public Tree Species page, the admin Trees / Master Inventory pages and
//  the officer "add tree to slot" dialog.
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/trees") @RequiredArgsConstructor
@Tag(name = "Tree Species")
public class TreeSpeciesController {

    private final TreeSpeciesService treeSpeciesService;

    @Value("${rolecode.plantation.admin}")
    private String adminRoleCode;

    @Value("${rolecode.plantation.horticultre-admin}")
    private String officerRoleCode;

    @Value("${rolecode.plantation.supervisor}")
    private String supervisorRoleCode;

    @GetMapping
    @Operation(summary = "List active tree species (public)")
    public ResponseEntity<ApiResponse<List<TreeSpeciesResponse>>> getActive() {
        return ResponseEntity.ok(ApiResponse.ok(treeSpeciesService.getAllActive()));
    }

    /**
     * Full catalog including deactivated species, for the staff screens.
     * "/trees/**" is publicly permitted in SecurityConfig, so the staff-only
     * rule has to be enforced here rather than by the URL matcher.
     */
    @GetMapping("/all")
    @Operation(summary = "List every tree species including inactive (MCD staff only)")
    public ResponseEntity<ApiResponse<List<TreeSpeciesResponse>>> getAll(Authentication authentication) {
        String roleCode = authentication != null && authentication.getPrincipal() instanceof UserToken token
            ? token.getRoleCode()
            : null;

        boolean staff = roleCode != null && (roleCode.equals(adminRoleCode)
            || roleCode.equals(officerRoleCode)
            || roleCode.equals(supervisorRoleCode));

        if (!staff) {
            throw new UnauthorizedException("Unauthorized Access, Only MCD staff can view all species");
        }
        return ResponseEntity.ok(ApiResponse.ok(treeSpeciesService.getAll()));
    }
}
