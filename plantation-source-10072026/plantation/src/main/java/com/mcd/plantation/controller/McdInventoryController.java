package com.mcd.plantation.controller;


import com.mcd.plantation.dto.request.*;
import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.pojo.UserToken;
import com.mcd.plantation.service.impl.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
// ════════════════════════════════════════════════════════════════
//  MCD INVENTORY CONTROLLER  — /mcd/inventory
// ════════════════════════════════════════════════════════════════
@RestController @RequestMapping("/mcd/inventory") @RequiredArgsConstructor
@Tag(name = "MCD — Inventory Management")
@SecurityRequirement(name = "bearerAuth")
//@PreAuthorize("hasAnyRole('HORTICULTURE_OFFICER','SUPERVISOR','ADMIN')")
public class McdInventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/matrix")
    @Operation(summary = "Get slot × tree inventory matrix for a park and date")
    public ResponseEntity<ApiResponse<InventoryMatrixResponse>> getMatrix(
    		Authentication authentication,
            @RequestParam UUID parkId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    	
    	UserToken userToken = (UserToken) authentication.getPrincipal();
    	String loginOfficerSystemCode = userToken.getUserSystemCode();
    	String roleCode = userToken.getRoleCode();
    	
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getMatrix(parkId, date, loginOfficerSystemCode, roleCode)));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get all low-stock and out-of-stock inventory alerts")
    public ResponseEntity<ApiResponse<List<LowStockAlert>>> getLowStock(
    		Authentication authentication
            //@AuthenticationPrincipal McdOfficial official
            ) {
    	
    	UserToken userToken = (UserToken) authentication.getPrincipal();
    	String loginOfficerSystemCode = userToken.getUserSystemCode();
    	String roleCode = userToken.getRoleCode();

        return ResponseEntity.ok(ApiResponse.ok(
                inventoryService.getLowStockAlerts(loginOfficerSystemCode, roleCode)));
    }

    @PostMapping
    @Operation(summary = "Assign a tree species to a slot with initial stock qty")
    public ResponseEntity<ApiResponse<SlotInventoryItem>> addTree(
    		Authentication authentication,
            @Valid @RequestBody AddInventoryRequest req) {
    	
    	UserToken userToken = (UserToken) authentication.getPrincipal();
    	String loginOfficerSystemCode = userToken.getUserSystemCode();
    	String roleCode = userToken.getRoleCode();
    	
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Tree added to slot", inventoryService.addTreeToSlot(req, loginOfficerSystemCode, roleCode)));
    }

    @PutMapping("/{inventoryId}")
    @Operation(summary = "Update stock quantity for a slot+tree inventory row")
    public ResponseEntity<ApiResponse<SlotInventoryItem>> updateStock(
    		Authentication authentication,
            @PathVariable UUID inventoryId,
            @Valid @RequestBody UpdateInventoryRequest req) {
    	
    	UserToken userToken = (UserToken) authentication.getPrincipal();
    	String loginOfficerSystemCode = userToken.getUserSystemCode();
    	String roleCode = userToken.getRoleCode();
    	
        return ResponseEntity.ok(ApiResponse.ok("Stock updated", inventoryService.updateStock(inventoryId, req,
        		loginOfficerSystemCode, roleCode)));
    }

    @DeleteMapping("/{inventoryId}")
    @Operation(summary = "Remove a tree from a slot (only if no active reservations)")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(
    		Authentication authentication,
            @PathVariable UUID inventoryId) {
    	
    	UserToken userToken = (UserToken) authentication.getPrincipal();
    	String loginOfficerSystemCode = userToken.getUserSystemCode();
    	String roleCode = userToken.getRoleCode();
    	
        inventoryService.deleteInventory(inventoryId, loginOfficerSystemCode, roleCode);
        return ResponseEntity.ok(ApiResponse.ok("Inventory removed", null));
    }
}