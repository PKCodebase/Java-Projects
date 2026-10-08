package com.mcd.plantation.repository;

import com.mcd.plantation.entity.SlotTreeInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SlotTreeInventoryRepository extends JpaRepository<SlotTreeInventory, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
	select i
	from SlotTreeInventory i
	where i.slot.slotId = :slotId
	and i.species.speciesId in :speciesIds
	""")
	List<SlotTreeInventory> findAllBySlotAndSpeciesWithLock(
	        UUID slotId,
	        List<UUID> speciesIds);
	
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM SlotTreeInventory i WHERE i.slot.slotId = :slotId AND i.species.speciesId = :speciesId")
    Optional<SlotTreeInventory> findBySlotAndSpeciesWithLock(
        @Param("slotId") UUID slotId,
        @Param("speciesId") UUID speciesId);

    @Query("SELECT i FROM SlotTreeInventory i JOIN FETCH i.species WHERE i.slot.slotId = :slotId ORDER BY i.species.commonName")
    List<SlotTreeInventory> findBySlotWithSpecies(@Param("slotId") UUID slotId);

    @Query("SELECT i FROM SlotTreeInventory i JOIN FETCH i.slot s JOIN FETCH s.park p JOIN FETCH i.species sp " +
           "WHERE s.slotDate >= :fromDate AND (i.stockQty - i.reservedQty) <= :threshold AND (i.stockQty - i.reservedQty) >= 0 " +
           "ORDER BY s.slotDate, p.name, s.startTime")
    List<SlotTreeInventory> findLowStock(@Param("threshold") int threshold, @Param("fromDate") LocalDate fromDate);

    @Query("SELECT i FROM SlotTreeInventory i JOIN FETCH i.slot s JOIN FETCH s.park p JOIN FETCH i.species " +
           "WHERE p.parkId = :parkId AND s.slotDate = :date ORDER BY s.startTime, i.species.commonName")
    List<SlotTreeInventory> findByParkAndDate(@Param("parkId") UUID parkId, @Param("date") LocalDate date);

    boolean existsBySlotSlotIdAndSpeciesSpeciesId(UUID slotId, UUID speciesId);

    @Query("SELECT COALESCE(SUM(i.stockQty), 0) FROM SlotTreeInventory i WHERE i.species.speciesId = :speciesId")
    int sumStockBySpecies(@Param("speciesId") UUID speciesId);

    @Query("SELECT COALESCE(SUM(i.stockQty), 0) FROM SlotTreeInventory i WHERE i.slot.slotId = :slotId")
    int sumStockBySlot(@Param("slotId") UUID slotId);

    @Query("SELECT COALESCE(SUM(i.stockQty), 0) FROM SlotTreeInventory i " +
           "WHERE i.slot.park.parkId = :parkId AND i.species.speciesId = :speciesId")
    int sumStockByParkAndSpecies(@Param("parkId") UUID parkId, @Param("speciesId") UUID speciesId);

    @Query("SELECT COALESCE(SUM(i.reservedQty), 0) FROM SlotTreeInventory i " +
           "WHERE i.slot.park.parkId = :parkId AND i.species.speciesId = :speciesId")
    int sumReservedByParkAndSpecies(@Param("parkId") UUID parkId, @Param("speciesId") UUID speciesId);

    @Query("SELECT i FROM SlotTreeInventory i JOIN FETCH i.slot s JOIN FETCH s.park p JOIN FETCH i.species sp " +
           "WHERE p.parkId IN :parkIds AND s.slotDate >= :fromDate " +
           "AND (i.stockQty - i.reservedQty) <= :threshold AND (i.stockQty - i.reservedQty) >= 0 " +
           "ORDER BY s.slotDate, p.name, s.startTime")
    List<SlotTreeInventory> findLowStockByParks(@Param("threshold") int threshold, @Param("parkIds") List<UUID> parkIds, @Param("fromDate") LocalDate fromDate);
}
