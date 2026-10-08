package com.mcd.plantation.controller;


import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mcd.plantation.dto.response.ApiResponse;
import com.mcd.plantation.dto.response.ParkSlotResponse;
import com.mcd.plantation.dto.response.SlotInventoryItem;
import com.mcd.plantation.service.impl.ParkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

// ════════════════════════════════════════════════════════════════
//  SLOT CONTROLLER  — /slots
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/slots") @RequiredArgsConstructor
@Tag(name = "Slots & Inventory")
public class SlotController {

    private final ParkService parkService;

    @GetMapping("/{slotId}")
    @Operation(summary = "Get slot detail with its tree inventory")
    public ResponseEntity<ApiResponse<ParkSlotResponse>> getSlot(@PathVariable UUID slotId) {
        return ResponseEntity.ok(ApiResponse.ok(parkService.getSlotDetail(slotId)));
    }

    @GetMapping("/{slotId}/inventory")
    @Operation(summary = "List trees available in a specific slot")
    public ResponseEntity<ApiResponse<List<SlotInventoryItem>>> getInventory(@PathVariable UUID slotId) {
        return ResponseEntity.ok(ApiResponse.ok(parkService.getSlotInventory(slotId)));
    }
}