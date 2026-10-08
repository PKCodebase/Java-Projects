package com.mcd.plantation.service.impl;

import com.mcd.plantation.dto.request.AllocateToParkRequest;
import com.mcd.plantation.dto.request.SetMasterStockRequest;
import com.mcd.plantation.dto.request.UpdateParkInventoryRequest;
import com.mcd.plantation.dto.response.MasterInventoryHistoryResponse;
import com.mcd.plantation.dto.response.MasterInventoryResponse;
import com.mcd.plantation.dto.response.ParkInventoryResponse;
import com.mcd.plantation.entity.*;
import com.mcd.plantation.exception.ResourceNotFoundException;
import com.mcd.plantation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class MasterInventoryService {

    private final MasterTreeInventoryRepository masterRepo;
    private final TreeSpeciesRepository speciesRepo;
    private final SlotTreeInventoryRepository slotInvRepo;
    private final ParkRepository parkRepo;
    private final ParkTreeInventoryRepository parkInvRepo;
    private final MasterInventoryHistoryRepository historyRepo;

    // ── Master stock ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MasterInventoryResponse> getAll() {
        Map<UUID, MasterTreeInventory> masterBySpecies = masterRepo.findAllWithSpecies().stream()
            .collect(Collectors.toMap(m -> m.getSpecies().getSpeciesId(), Function.identity()));

        return speciesRepo.findAllByOrderByCommonName().stream()
            .map(sp -> {
                MasterTreeInventory m = masterBySpecies.get(sp.getSpeciesId());
                if (m != null) {
                    int allocated = slotInvRepo.sumStockBySpecies(sp.getSpeciesId());
                    return toResponse(m, allocated);
                }
                return new MasterInventoryResponse(
                    null, sp.getSpeciesId(), sp.getCommonName(), sp.getScientificName(),
                    sp.getEmojiCode(), sp.getCategory(), sp.getPrice(), 0, 0, 0);
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public MasterInventoryResponse upsert(SetMasterStockRequest req, String official) {
        TreeSpecies sp = speciesRepo.findById(req.speciesId())
            .orElseThrow(() -> ResourceNotFoundException.of("TreeSpecies", req.speciesId()));
        MasterTreeInventory inv = masterRepo.findBySpecies_SpeciesId(req.speciesId())
            .orElse(MasterTreeInventory.builder().species(sp).build());
        inv.setTotalQty(req.qty());
        masterRepo.save(inv);

        recordHistory("STOCK_SET", sp, req.qty(), null, official);

        return toResponse(inv, slotInvRepo.sumStockBySpecies(req.speciesId()));
    }

    /** Returns available qty in master pool for a species (slot-based, for backward compat). */
    @Transactional(readOnly = true)
    public int getAvailable(UUID speciesId) {
        return masterRepo.findBySpecies_SpeciesId(speciesId)
            .map(m -> Math.max(0, m.getTotalQty() - slotInvRepo.sumStockBySpecies(speciesId)))
            .orElse(Integer.MAX_VALUE);
    }

    // ── Park allocation ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ParkInventoryResponse> getParkInventory(UUID parkId) {
        if (!parkRepo.existsById(parkId))
            throw ResourceNotFoundException.of("Park", parkId);
        return parkInvRepo.findByParkIdWithSpecies(parkId).stream()
            .map(p -> {
                UUID sid = p.getSpecies().getSpeciesId();
                int used   = slotInvRepo.sumStockByParkAndSpecies(parkId, sid);
                int booked = slotInvRepo.sumReservedByParkAndSpecies(parkId, sid);
                return toParkInvResponse(p, used, booked);
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public ParkInventoryResponse allocateToPark(UUID parkId, AllocateToParkRequest req, 
    		String official) {
        Park park = parkRepo.findById(parkId)
            .orElseThrow(() -> ResourceNotFoundException.of("Park", parkId));
        TreeSpecies sp = speciesRepo.findById(req.speciesId())
            .orElseThrow(() -> ResourceNotFoundException.of("TreeSpecies", req.speciesId()));

        int masterTotal = masterRepo.findBySpecies_SpeciesId(req.speciesId())
            .map(MasterTreeInventory::getTotalQty).orElse(0);
        int totalAllocated = parkInvRepo.sumAllocatedBySpecies(req.speciesId());

        ParkTreeInventory existing = parkInvRepo
            .findByPark_ParkIdAndSpecies_SpeciesId(parkId, req.speciesId()).orElse(null);
        int currentParkAlloc = existing != null ? existing.getAllocatedQty() : 0;

        // otherParks + this park's new qty must not exceed master total
        int otherParksAllocated = totalAllocated - currentParkAlloc;
        if (otherParksAllocated + req.qty() > masterTotal) {
            int available = Math.max(0, masterTotal - otherParksAllocated);
            throw new IllegalArgumentException(
                "Only " + available + " available in master pool for " + sp.getCommonName()
                + " (master total: " + masterTotal + ", other parks using: " + otherParksAllocated + ")");
        }

        ParkTreeInventory parkInv;
        String action;
        if (existing != null) {
            existing.setAllocatedQty(req.qty());
            parkInv = parkInvRepo.save(existing);
            action = "PARK_UPDATED";
        } else {
            parkInv = parkInvRepo.save(ParkTreeInventory.builder()
                .park(park).species(sp).allocatedQty(req.qty()).build());
            action = "PARK_ALLOCATED";
        }

        recordHistory(action, sp, req.qty(), park, official);
        int used   = slotInvRepo.sumStockByParkAndSpecies(parkId, req.speciesId());
        int booked = slotInvRepo.sumReservedByParkAndSpecies(parkId, req.speciesId());
        return toParkInvResponse(parkInv, used, booked);
    }

    @Transactional
    public ParkInventoryResponse updateParkInventory(UUID parkId, UUID parkInvId, UpdateParkInventoryRequest req, 
    		String official) {
        ParkTreeInventory inv = parkInvRepo.findById(parkInvId)
            .orElseThrow(() -> ResourceNotFoundException.of("ParkInventory", parkInvId));
        if (!inv.getPark().getParkId().equals(parkId))
            throw new IllegalArgumentException("Inventory does not belong to this park");

        int used = slotInvRepo.sumStockByParkAndSpecies(parkId, inv.getSpecies().getSpeciesId());
        if (req.qty() < used)
            throw new IllegalArgumentException(
                "Cannot reduce to " + req.qty() + "; " + used + " already used in slots");

        int masterTotal = masterRepo.findBySpecies_SpeciesId(inv.getSpecies().getSpeciesId())
            .map(MasterTreeInventory::getTotalQty).orElse(0);
        int alreadyAllocated = parkInvRepo.sumAllocatedBySpecies(inv.getSpecies().getSpeciesId());
        int netChange = req.qty() - inv.getAllocatedQty();
        if (netChange > 0 && (alreadyAllocated + netChange) > masterTotal)
            throw new IllegalArgumentException("Increase exceeds master pool remaining capacity");

        inv.setAllocatedQty(req.qty());
        parkInvRepo.save(inv);
        recordHistory("PARK_UPDATED", inv.getSpecies(), req.qty(), inv.getPark(), official);
        int booked = slotInvRepo.sumReservedByParkAndSpecies(parkId, inv.getSpecies().getSpeciesId());
        return toParkInvResponse(inv, used, booked);
    }

    @Transactional
    public void removeParkInventory(UUID parkId, UUID parkInvId, String official) {
        ParkTreeInventory inv = parkInvRepo.findById(parkInvId)
            .orElseThrow(() -> ResourceNotFoundException.of("ParkInventory", parkInvId));
        if (!inv.getPark().getParkId().equals(parkId))
            throw new IllegalArgumentException("Inventory does not belong to this park");

        int used = slotInvRepo.sumStockByParkAndSpecies(parkId, inv.getSpecies().getSpeciesId());
        if (used > 0)
            throw new IllegalStateException(
                used + " units of " + inv.getSpecies().getCommonName() + " already used in slots; remove slot inventory first");

        recordHistory("PARK_REMOVED", inv.getSpecies(), inv.getAllocatedQty(), inv.getPark(), official);
        parkInvRepo.delete(inv);
    }

    // ── History ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MasterInventoryHistoryResponse> getHistory() {
        return historyRepo.findAllOrderByPerformedAtDesc().stream()
            .map(h -> new MasterInventoryHistoryResponse(
                h.getHistoryId(),
                h.getAction(),
                h.getSpecies().getSpeciesId(),
                h.getSpecies().getCommonName(),
                h.getSpecies().getEmojiCode(),
                h.getQty(),
                h.getPark() != null ? h.getPark().getParkId() : null,
                h.getPark() != null ? h.getPark().getName() : null,
                h.getPerformedBy() != null ? h.getPerformedBy() : "System",
                h.getPerformedAt()
            ))
            .collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────────────

    private void recordHistory(String action, TreeSpecies sp, int qty, Park park, String official) {
        historyRepo.save(MasterInventoryHistory.builder()
            .action(action).species(sp).qty(qty).park(park).performedBy(official).build());
    }

    private MasterInventoryResponse toResponse(MasterTreeInventory m, int allocated) {
        return new MasterInventoryResponse(
            m.getMasterInvId(),
            m.getSpecies().getSpeciesId(),
            m.getSpecies().getCommonName(),
            m.getSpecies().getScientificName(),
            m.getSpecies().getEmojiCode(),
            m.getSpecies().getCategory(),
            m.getSpecies().getPrice(),
            m.getTotalQty(),
            allocated,
            Math.max(0, m.getTotalQty() - allocated)
        );
    }

    private ParkInventoryResponse toParkInvResponse(ParkTreeInventory p, int usedQty, int bookedQty) {
        TreeSpecies sp = p.getSpecies();
        return new ParkInventoryResponse(
            p.getParkInvId(),
            p.getPark().getParkId(),
            p.getPark().getName(),
            sp.getSpeciesId(),
            sp.getCommonName(),
            sp.getScientificName(),
            sp.getEmojiCode(),
            sp.getCategory(),
            p.getAllocatedQty(),
            usedQty,
            bookedQty,
            Math.max(0, p.getAllocatedQty() - usedQty)
        );
    }
}
