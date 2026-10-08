/*
 * package com.mcd.plantation.controller;
 * 
 * 
 * import com.mcd.plantation.dto.request.*; import
 * com.mcd.plantation.dto.response.*; import
 * io.swagger.v3.oas.annotations.Operation; import
 * io.swagger.v3.oas.annotations.security.SecurityRequirement; import
 * io.swagger.v3.oas.annotations.tags.Tag; import jakarta.validation.Valid;
 * import lombok.RequiredArgsConstructor; import org.springframework.http.*;
 * import org.springframework.web.bind.annotation.*; import java.util.UUID;
 * 
 * 
 * @RestController @RequestMapping("/mcd/wards") @RequiredArgsConstructor
 * 
 * @Tag(name = "MCD — Zone & Ward Management")
 * 
 * @SecurityRequirement(name = "bearerAuth")
 * //??@PreAuthorize("hasRole('ADMIN')") public class McdWardController {
 * 
 * private final com.mcd.plantation.service.impl.ZoneWardService
 * zoneWardService;
 * 
 * @PostMapping
 * 
 * @Operation(summary = "Create a new ward within a zone") public
 * ResponseEntity<ApiResponse<WardResponse>> createWard(
 * 
 * @Valid @RequestBody CreateWardRequest req) { return
 * ResponseEntity.status(HttpStatus.CREATED)
 * .body(ApiResponse.ok("Ward created", zoneWardService.createWard(req))); }
 * 
 * @PutMapping("/{wardId}")
 * 
 * @Operation(summary = "Update a ward") public
 * ResponseEntity<ApiResponse<WardResponse>> updateWard(
 * 
 * @PathVariable UUID wardId,
 * 
 * @Valid @RequestBody CreateWardRequest req) { return
 * ResponseEntity.ok(ApiResponse.ok("Ward updated",
 * zoneWardService.updateWard(wardId, req))); }
 * 
 * @DeleteMapping("/{wardId}")
 * 
 * @Operation(summary = "Deactivate a ward") public
 * ResponseEntity<ApiResponse<Void>> deactivateWard(@PathVariable UUID wardId) {
 * zoneWardService.deactivateWard(wardId); return
 * ResponseEntity.ok(ApiResponse.ok("Ward deactivated", null)); } }
 */