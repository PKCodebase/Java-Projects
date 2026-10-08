package com.mcd.plantation.service.impl;

import com.mcd.plantation.dto.request.AddInventoryRequest;
import com.mcd.plantation.dto.request.UpdateInventoryRequest;
import com.mcd.plantation.dto.response.*;
import com.mcd.plantation.entity.*;
import com.mcd.plantation.enums.OfficialRole;
import com.mcd.plantation.exception.DuplicateResourceException;
import com.mcd.plantation.exception.ResourceNotFoundException;
import com.mcd.plantation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class InventoryService {

    private final SlotTreeInventoryRepository inventoryRepo;
    private final ParkSlotRepository slotRepo;
    private final TreeSpeciesRepository speciesRepo;
    private final ParkRepository parkRepo;
    private final ParkOfficialAssignmentRepository assignmentRepo;
    private final MasterInventoryService masterInventoryService;
    private final ParkTreeInventoryRepository parkInvRepo;

    @Value("${mcd.inventory.low-stock-threshold}") private int threshold;

    @Transactional
    public SlotInventoryItem addTreeToSlot(AddInventoryRequest req, String offcialId, String roleCode) {
        ParkSlot slot = slotRepo.findById(req.slotId())
            .orElseThrow(() -> ResourceNotFoundException.of("Slot", req.slotId()));

        assertAssigned(slot.getPark().getParkId(), offcialId, roleCode);

        if (inventoryRepo.existsBySlotSlotIdAndSpeciesSpeciesId(req.slotId(), req.speciesId()))
            throw new DuplicateResourceException("Species already assigned to this slot.");

        TreeSpecies sp = speciesRepo.findById(req.speciesId())
            .orElseThrow(() -> ResourceNotFoundException.of("TreeSpecies", req.speciesId()));

        // Validate against park-level allocation (must be allocated to the park first)
        ParkTreeInventory parkInv = parkInvRepo
            .findByPark_ParkIdAndSpecies_SpeciesId(slot.getPark().getParkId(), req.speciesId())
            .orElseThrow(() -> new IllegalArgumentException(
                sp.getCommonName() + " is not allocated to this park. Add it via Park Inventory first."));
        int alreadyUsedInPark = inventoryRepo.sumStockByParkAndSpecies(slot.getPark().getParkId(), req.speciesId());
        int availableForPark = parkInv.getAllocatedQty() - alreadyUsedInPark;
        if (req.stockQty() > availableForPark)
            throw new IllegalArgumentException(
                "Requested qty (" + req.stockQty() + ") exceeds park allocation available ("
                + availableForPark + " of " + parkInv.getAllocatedQty() + " allocated).");

        // Slot capacity: total trees across all species must not exceed slot capacity
        int currentSlotTotal = inventoryRepo.sumStockBySlot(req.slotId());
        if (currentSlotTotal + req.stockQty() > slot.getCapacity())
            throw new IllegalArgumentException(
                "Adding " + req.stockQty() + " would exceed slot capacity (" + slot.getCapacity()
                + "); currently " + currentSlotTotal + " allocated to this slot.");

        SlotTreeInventory inv = SlotTreeInventory.builder()
            .slot(slot).species(sp).stockQty(req.stockQty()).reservedQty(0).build();
        inventoryRepo.save(inv);
        return ParkService.toInventoryItem(inv, threshold);
    }

    @Transactional
    public SlotInventoryItem updateStock(UUID inventoryId, UpdateInventoryRequest req, String offcialId, String roleCode) {
        SlotTreeInventory inv = inventoryRepo.findById(inventoryId)
            .orElseThrow(() -> ResourceNotFoundException.of("Inventory", inventoryId));

        assertAssigned(inv.getSlot().getPark().getParkId(), offcialId, roleCode);

        if (req.stockQty() < inv.getReservedQty())
            throw new IllegalArgumentException(
                "Cannot set stock below current reserved qty (" + inv.getReservedQty() + ")");

        int delta = req.stockQty() - inv.getStockQty();
        if (delta > 0) {
            int available = masterInventoryService.getAvailable(inv.getSpecies().getSpeciesId());
            if (available < delta)
                throw new IllegalArgumentException(
                    "Cannot increase stock by " + delta + "; only " + available + " available in master inventory.");

            // Slot capacity: other species in slot + new qty must not exceed capacity
            int currentSlotTotal = inventoryRepo.sumStockBySlot(inv.getSlot().getSlotId());
            int newSlotTotal = currentSlotTotal - inv.getStockQty() + req.stockQty();
            if (newSlotTotal > inv.getSlot().getCapacity())
                throw new IllegalArgumentException(
                    "Update would exceed slot capacity (" + inv.getSlot().getCapacity()
                    + "); other species use " + (currentSlotTotal - inv.getStockQty()) + " already.");
        }

        inv.setStockQty(req.stockQty());
        inventoryRepo.save(inv);
        return ParkService.toInventoryItem(inv, threshold);
    }

    @Transactional
    public void deleteInventory(UUID inventoryId, String official, String roleCode) {
        SlotTreeInventory inv = inventoryRepo.findById(inventoryId)
            .orElseThrow(() -> ResourceNotFoundException.of("Inventory", inventoryId));

        assertAssigned(inv.getSlot().getPark().getParkId(), official, roleCode);

        if (inv.getReservedQty() > 0)
            throw new IllegalStateException("Cannot remove inventory with active reservations.");
        inventoryRepo.delete(inv);
    }

    public List<LowStockAlert> getLowStockAlerts(String official, String roleCode) {
        LocalDate today = LocalDate.now();
        List<SlotTreeInventory> items;
        // Admin gets every park's low-stock rows; everyone else (officer/supervisor)
        // is scoped to the parks assigned to them. The scope decision must be made
        // on the ROLE CODE — `official` is the login system code (employee id) and
        // never equals a role name, so comparing it here silently demoted admins
        // to the assignment-scoped branch (which is always empty for an admin).
        if (OfficialRole.R_HORTIC_ADM.name().equals(roleCode)) {
            items = inventoryRepo.findLowStock(threshold, today);
        } else {
            List<UUID> parkIds = assignmentRepo.findParkIdsByOfficialId(official);
            items = parkIds.isEmpty() ? List.of() : inventoryRepo.findLowStockByParks(threshold, parkIds, today);
        }
        return items.stream()
            .map(i -> new LowStockAlert(
                i.getInventoryId(), i.getSlot().getSlotId(),
                i.getSlot().getSlotDate(), i.getSlot().getStartTime(),
                i.getSlot().getPark().getParkId(), i.getSlot().getPark().getName(),
                i.getSpecies().getSpeciesId(), i.getSpecies().getCommonName(),
                i.getSpecies().getEmojiCode(),
                i.getStockQty(), i.getReservedQty(), i.getAvailableQty()))
            .collect(Collectors.toList());
    }

    public InventoryMatrixResponse getMatrix(UUID parkId, LocalDate date, String official, String roleCode) {
        assertAssigned(parkId, official, roleCode);

        Park park = parkRepo.findById(parkId)
            .orElseThrow(() -> ResourceNotFoundException.of("Park", parkId));

        // All slots for this park+date — including those with no inventory assigned yet
        List<ParkSlot> slots = slotRepo.findByParkAndDate(parkId, date);

        // All inventory rows for this park+date
        List<SlotTreeInventory> all = inventoryRepo.findByParkAndDate(parkId, date);

        // Ordered species list derived from inventory (alphabetical)
        List<String> speciesOrder = all.stream()
            .map(i -> i.getSpecies().getCommonName())
            .distinct().sorted().collect(Collectors.toList());

        // Group inventory by slotId → speciesName for O(1) cell lookup
        Map<UUID, Map<String, SlotTreeInventory>> bySlotAndSpecies = new HashMap<>();
        for (SlotTreeInventory i : all) {
            bySlotAndSpecies
                .computeIfAbsent(i.getSlot().getSlotId(), k -> new HashMap<>())
                .put(i.getSpecies().getCommonName(), i);
        }

        // Build one row per slot; cells are aligned to speciesOrder (null when species absent)
        List<SlotMatrixRow> rows = slots.stream()
            .map(slot -> {
                Map<String, SlotTreeInventory> slotInv =
                    bySlotAndSpecies.getOrDefault(slot.getSlotId(), Collections.emptyMap());
                List<InventoryCell> cells = speciesOrder.stream()
                    .map(spName -> {
                        SlotTreeInventory inv = slotInv.get(spName);
                        if (inv == null) return null;
                        return new InventoryCell(
                            inv.getInventoryId(), inv.getSpecies().getSpeciesId(),
                            inv.getStockQty(), inv.getReservedQty(), inv.getAvailableQty(),
                            SlotInventoryItem.stockStatus(inv.getAvailableQty(), threshold));
                    })
                    .collect(Collectors.toList());
                return new SlotMatrixRow(
                    slot.getSlotId(), slot.getStartTime(), slot.getEndTime(),
                    slot.getCapacity(), slot.getBookedCount(), slot.getFreeSpots(),
                    slot.getStatus(), cells);
            })
            .sorted(Comparator.comparing(SlotMatrixRow::startTime))
            .collect(Collectors.toList());

        return new InventoryMatrixResponse(parkId, park.getName(), date, speciesOrder, rows);
    }

    private void assertAssigned(UUID parkId, String officialId, String roleCode) {
        // Admin aur Supervisor ko kisi bhi park ka access hai
        if (roleCode.equals(OfficialRole.R_HORTIC_ADM.name())
                || roleCode.equals(OfficialRole.SUPERVISOR.name())) return;
        // Officer ke liye assignment check karo
        if (!assignmentRepo.existsByPark_ParkIdAndOfficial(parkId, officialId))
            throw new AccessDeniedException("You are not assigned to park " + parkId);
    }
}
