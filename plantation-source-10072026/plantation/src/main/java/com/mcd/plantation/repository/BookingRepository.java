package com.mcd.plantation.repository;

import com.mcd.plantation.entity.Booking;
import com.mcd.plantation.enums.BookingStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

	
	 @Query("SELECT b FROM Booking b " +
	           "LEFT JOIN FETCH b.items i " +
	           "LEFT JOIN FETCH i.inventory inv " +
	           "LEFT JOIN FETCH inv.species " +
	           "LEFT JOIN FETCH b.slot s " +
	           "LEFT JOIN FETCH s.park " +
	           "LEFT JOIN FETCH b.occasion " +
	           "WHERE b.bookingId = :id")
	    Optional<Booking> findByIdWithDetails(@Param("id") UUID id);
	 
	 
	@Query("""
	        SELECT DISTINCT b
	        FROM Booking b
	        JOIN FETCH b.slot s
	        JOIN FETCH s.park
	        JOIN FETCH b.items bi
	        JOIN FETCH bi.inventory i
	        JOIN FETCH i.species
	        WHERE b.citizen = :citizenId
	        ORDER BY b.bookedAt DESC
	    """)
	    List<Booking> findByCitizenWithDetails(@Param("citizenId") String citizenId);

    Optional<Booking> findByBookingRef(String bookingRef);

    @Query("""
            SELECT DISTINCT b
            FROM Booking b
            JOIN FETCH b.slot s
            JOIN FETCH s.park
            JOIN FETCH b.items bi
            JOIN FETCH bi.inventory i
            JOIN FETCH i.species
            WHERE b.status = :status
            ORDER BY b.bookedAt
        """)
    List<Booking> findByStatus(@Param("status") BookingStatus status);

    @Query("""
            SELECT DISTINCT b
            FROM Booking b
            JOIN FETCH b.slot s
            JOIN FETCH s.park
            JOIN FETCH b.items bi
            JOIN FETCH bi.inventory i
            JOIN FETCH i.species
            WHERE s.slotDate = :date
            ORDER BY s.startTime
        """)
        List<Booking> findBySlotDate(@Param("date") LocalDate date);

    long countByCitizen(String citizen);

    @Query("""
            SELECT DISTINCT b
            FROM Booking b
            JOIN FETCH b.slot s
            JOIN FETCH s.park
            JOIN FETCH b.items bi
            JOIN FETCH bi.inventory i
            JOIN FETCH i.species
            WHERE b.status = :status
              AND s.park.parkId IN :parkIds
            ORDER BY b.bookedAt
        """)
        List<Booking> findByStatusAndParkIds(
                @Param("status") BookingStatus status,
                @Param("parkIds") List<UUID> parkIds);

		/*
		 * @Query(""" SELECT DISTINCT b FROM Booking b JOIN FETCH b.slot s JOIN FETCH
		 * s.park JOIN FETCH b.items bi JOIN FETCH bi.inventory i JOIN FETCH i.species
		 * WHERE b.status IN :statuses ORDER BY s.slotDate,s.startTime """)
		 */
    	// When :fromDate IS NULL OR :toDate IS NULL THEN OR condition of 
        // s.slotDate BETWEEN :fromDate AND :toDate will not be checked
    	@Query("""
    	    SELECT DISTINCT b
    	    FROM Booking b
    	    JOIN FETCH b.slot s
    	    JOIN FETCH s.park
    	    JOIN FETCH b.items bi
    	    JOIN FETCH bi.inventory i
    	    JOIN FETCH i.species
    	    WHERE b.status IN :statuses
    	    AND (cast(:fromDate as localdate) IS NULL OR cast(:toDate as localdate) IS NULL OR s.slotDate BETWEEN :fromDate AND :toDate)
    	    ORDER BY s.slotDate ASC, s.startTime ASC
    	""")
        List<Booking> findByStatusIn(@Param("statuses") List<BookingStatus> statuses, 
        		@Param("fromDate") LocalDate fromDate, 
                @Param("toDate") LocalDate toDate);
    
    @Query("""
            SELECT DISTINCT b
            FROM Booking b
            JOIN FETCH b.slot s
            JOIN FETCH s.park
            JOIN FETCH b.items bi
            JOIN FETCH bi.inventory i
            JOIN FETCH i.species
            WHERE b.status IN :statuses
              AND s.slotDate = :slotDate
            ORDER BY s.startTime
        """)
        List<Booking> findByStatusInAndSlotDate(
                @Param("statuses") List<BookingStatus> statuses,
                @Param("slotDate") LocalDate slotDate);

    @Query("""
            SELECT DISTINCT b
            FROM Booking b
            JOIN FETCH b.slot s
            JOIN FETCH s.park
            JOIN FETCH b.items bi
            JOIN FETCH bi.inventory i
            JOIN FETCH i.species
            WHERE b.status IN :statuses
            AND (cast(:fromDate as localdate) IS NULL OR cast(:toDate as localdate) IS NULL OR s.slotDate BETWEEN :fromDate AND :toDate)
              AND s.park.parkId IN :parkIds
            ORDER BY s.slotDate,s.startTime
        """)
        List<Booking> findByStatusInAndParkIds(
                @Param("statuses") List<BookingStatus> statuses,
                @Param("parkIds") List<UUID> parkIds,
                @Param("fromDate") LocalDate fromDate, 
                @Param("toDate") LocalDate toDate);
    
    @Query("""
            SELECT DISTINCT b
            FROM Booking b
            JOIN FETCH b.slot s
            JOIN FETCH s.park
            JOIN FETCH b.items bi
            JOIN FETCH bi.inventory i
            JOIN FETCH i.species
            WHERE b.status IN :statuses
              AND s.slotDate = :slotDate
              AND s.park.parkId IN :parkIds
            ORDER BY s.startTime
        """)
        List<Booking> findByStatusInAndParkIdsAndSlotDate(
                @Param("statuses") List<BookingStatus> statuses,
                @Param("slotDate") LocalDate slotDate,
                @Param("parkIds") List<UUID> parkIds);

    // ── Dashboard aggregate queries ─────────────────────────────

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    long countByBookingStatus(@Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookedAt >= :start AND b.bookedAt < :end")
    long countByBookedAtRange(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    /** Zone → total bookings count */
    @Query("""
            SELECT p.zone, COUNT(DISTINCT b)
            FROM Booking b
            JOIN b.slot s
            JOIN s.park p
            GROUP BY p.zone
            ORDER BY p.zone
        """)
        List<Object[]> countByZone();

    /** Zone → bookings count filtered by status list */
        @Query("""
                SELECT p.zone, COUNT(DISTINCT b)
                FROM Booking b
                JOIN b.slot s
                JOIN s.park p
                WHERE b.status IN :statuses
                GROUP BY p.zone
                ORDER BY p.zone
            """)
            List<Object[]> countByZoneAndStatus(
                    @Param("statuses") List<BookingStatus> statuses);

    /** Species → bookings count filtered by status (top N via Pageable) */
            @Query("""
                    SELECT sp.commonName,
                           sp.scientificName,
                           sp.emojiCode,
                           SUM(bi.quantity)
                    FROM Booking b
                    JOIN b.items bi
                    JOIN bi.inventory i
                    JOIN i.species sp
                    WHERE b.status IN :statuses
                    GROUP BY sp.commonName,
                             sp.scientificName,
                             sp.emojiCode
                    ORDER BY SUM(bi.quantity) DESC
                """)
                List<Object[]> countBySpeciesAndStatus(
                        @Param("statuses") List<BookingStatus> statuses,
                        Pageable pageable);

    /** Park (+ zone) → pending bookings count (top N via Pageable) */
                @Query("""
                        SELECT p.name,
                               COALESCE(p.zone),
                               COUNT(DISTINCT b)
                        FROM Booking b
                        JOIN b.slot s
                        JOIN s.park p
                        WHERE b.status IN :statuses
                        GROUP BY p.name,p.zone
                        ORDER BY COUNT(DISTINCT b) DESC
                    """)
                    List<Object[]> countPendingByPark(
                            @Param("statuses") List<BookingStatus> statuses,
                            Pageable pageable);

    /** Slot date → bookings count (for daily booked trend) */
                    @Query("""
                            SELECT s.slotDate,
                                   COUNT(DISTINCT b)
                            FROM Booking b
                            JOIN b.slot s
                            WHERE s.slotDate >= :start
                            GROUP BY s.slotDate
                            ORDER BY s.slotDate
                        """)
                        List<Object[]> countBySlotDate(@Param("start") LocalDate start);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM Booking s
            WHERE s.bookingId = :bookingId
           """)
	Optional<Booking> findByIdForUpdate(UUID bookingId);
}
