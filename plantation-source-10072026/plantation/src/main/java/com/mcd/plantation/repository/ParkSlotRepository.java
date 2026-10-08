package com.mcd.plantation.repository;

import com.mcd.plantation.entity.ParkSlot;
import com.mcd.plantation.enums.SlotStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParkSlotRepository extends JpaRepository<ParkSlot, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM ParkSlot s
            WHERE s.slotId = :slotId
           """)
    Optional<ParkSlot> findByIdForUpdate(@Param("slotId") UUID slotId);
	
    @Query("SELECT s FROM ParkSlot s WHERE s.park.parkId = :parkId AND s.slotDate = :date ORDER BY s.startTime")
    List<ParkSlot> findByParkAndDate(@Param("parkId") UUID parkId, @Param("date") LocalDate date);

    @Query("SELECT s FROM ParkSlot s WHERE s.park.parkId = :parkId AND s.slotDate = :date AND s.status = :status ORDER BY s.startTime")
    List<ParkSlot> findByParkDateAndStatus(@Param("parkId") UUID parkId, @Param("date") LocalDate date, @Param("status") SlotStatus status);

    @Query("SELECT s FROM ParkSlot s WHERE s.park.parkId = :parkId ORDER BY s.slotDate, s.startTime")
    List<ParkSlot> findByPark(@Param("parkId") UUID parkId);

    @Query("SELECT COUNT(s) FROM ParkSlot s WHERE s.park.parkId = :parkId AND s.slotDate = :date " +
           "AND s.slotId <> :excludeId AND s.startTime < :endTime AND s.endTime > :startTime")
    long countOverlapping(@Param("parkId") UUID parkId, @Param("date") LocalDate date,
                          @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime,
                          @Param("excludeId") UUID excludeId);

    /** Count active (non-CLOSED) slots within the next N days — used by the dashboard. */
    @Query("SELECT COUNT(s) FROM ParkSlot s " +
           "WHERE s.slotDate >= :today AND s.slotDate <= :futureDate AND s.status <> :closedStatus")
    long countUpcomingActiveSlots(@Param("today") LocalDate today,
                                   @Param("futureDate") LocalDate futureDate,
                                   @Param("closedStatus") SlotStatus closedStatus);
}
