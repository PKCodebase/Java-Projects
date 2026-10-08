package com.mcd.plantation.controller;

import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.service.impl.ZoneWardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


// ════════════════════════════════════════════════════════════════
//  ZONE CONTROLLER (public)  — /zones
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/zones") @RequiredArgsConstructor
@Tag(name = "Zones")
public class ZoneController {

    private final ZoneWardService zoneWardService;

    @GetMapping
    @Operation(summary = "List all active zones")
    public ResponseEntity<ApiResponse<List<ZoneResponse>>> getActiveZones() {
        return ResponseEntity.ok(ApiResponse.ok(zoneWardService.getActiveZones()));
    }

}