package com.mcd.plantation.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcd.plantation.dto.response.ApiResponse;
import com.mcd.plantation.dto.response.ParkResponse;
import com.mcd.plantation.dto.response.ParkSlotResponse;
import com.mcd.plantation.service.impl.ParkService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

//════════════════════════════════════════════════════════════════
//PARK CONTROLLER  — /parks
//════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/parks") @RequiredArgsConstructor
@Tag(name = "Parks & Slots")
public class ParkController {

		private final ParkService parkService;
		
		@GetMapping
		@Operation(summary = "List active parks — paginated (default page=0, size=9)")
		public ResponseEntity<ApiResponse<Page<ParkResponse>>> getAllParks(
				Authentication authentication,
		        @RequestParam(required = false) String zoneId,
		        @RequestParam(defaultValue = "0")  int page,
		        @RequestParam(defaultValue = "9")  int size) {
		    return ResponseEntity.ok(ApiResponse.ok(parkService.getParksPageable(zoneId, page, size)));
		}
		
		@GetMapping("/all")
		@Operation(summary = "List all active parks without pagination (for booking dropdowns)")
		public ResponseEntity<ApiResponse<List<ParkResponse>>> getAllParksFlat(
		        @RequestParam(required = false) String zoneId) {
		    return ResponseEntity.ok(ApiResponse.ok(parkService.getAllParks(zoneId)));
		}
		
		@GetMapping("/{parkId}")
		@Operation(summary = "Get park detail")
		public ResponseEntity<ApiResponse<ParkResponse>> getPark(@PathVariable UUID parkId) {
		    return ResponseEntity.ok(ApiResponse.ok(parkService.getPark(parkId)));
		}
		
		@GetMapping("/{parkId}/slots")
		@Operation(summary = "Get slots for a park on a given date — includes per-slot tree inventory")
		public ResponseEntity<ApiResponse<List<ParkSlotResponse>>> getSlots(
		        @PathVariable UUID parkId,
		        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		    return ResponseEntity.ok(ApiResponse.ok(parkService.getSlotsForPark(parkId, date)));
		}
}
