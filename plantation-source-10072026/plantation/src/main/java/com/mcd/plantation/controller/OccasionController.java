package com.mcd.plantation.controller;


import com.mcd.plantation.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


// ════════════════════════════════════════════════════════════════
//  PUBLIC OCCASIONS CONTROLLER  — /occasions
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/occasions") @RequiredArgsConstructor
@Tag(name = "Occasions")
public class OccasionController {

    private final com.mcd.plantation.service.impl.OccasionService occasionService;

    @GetMapping
    @Operation(summary = "List all active occasions")
    public ResponseEntity<ApiResponse<List<com.mcd.plantation.dto.response.OccasionResponse>>> getAll(
    		Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(occasionService.getAll()));
    }

    @GetMapping("/{occasionId}/species")
    @Operation(summary = "List active tree species recommended for a given occasion")
    public ResponseEntity<ApiResponse<List<com.mcd.plantation.dto.response.TreeSpeciesResponse>>> getSpeciesForOccasion(
            @PathVariable UUID occasionId,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(occasionService.getSpeciesForOccasion(occasionId)));
    }
}