package com.mcd.plantation.repository;

import com.mcd.plantation.entity.PlantationRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlantationRecordRepository extends JpaRepository<PlantationRecord, UUID> {
    Optional<PlantationRecord> findByBookingBookingId(UUID bookingId);

    @Query("SELECT r FROM PlantationRecord r WHERE r.official = :officialId ORDER BY r.recordedAt DESC")
    List<PlantationRecord> findByOfficial(@Param("officialId") String officialId);

    @Query("SELECT r FROM PlantationRecord r " +
           "JOIN FETCH r.booking b  " +
           "JOIN FETCH b.slot s JOIN FETCH s.park p JOIN FETCH b.items bi JOIN FETCH bi.inventory inv "
           + " JOIN FETCH inv.species\n"
           + "	\n"
           + "	 " +
           //"LEFT JOIN FETCH r.official o " +
           "WHERE p.parkId IN :parkIds "
           + "AND (cast(:fromDate as localdate) IS NULL OR cast(:toDate as localdate) IS NULL OR r.plantedDate BETWEEN :fromDate AND :toDate) "
           + " ORDER BY r.recordedAt DESC")
    List<PlantationRecord> findByParkIds(@Param("parkIds") List<UUID> parkIds,
    		@Param("fromDate") LocalDate fromDate, 
            @Param("toDate") LocalDate toDate);

    
    @Query("SELECT r FROM PlantationRecord r " +
           "JOIN FETCH r.booking b  " 
    	   + "JOIN FETCH b.slot s JOIN FETCH s.park JOIN FETCH b.items bi JOIN FETCH bi.inventory inv "
           + "JOIN FETCH inv.species\n"
           + "	WHERE (cast(:fromDate as localdate) IS NULL OR cast(:toDate as localdate) IS NULL OR r.plantedDate BETWEEN :fromDate AND :toDate)"
           + "ORDER BY r.recordedAt DESC")
    List<PlantationRecord> findAllWithDetails(@Param("fromDate") LocalDate fromDate, 
            @Param("toDate") LocalDate toDate);

    // ── Dashboard aggregate queries ─────────────────────────────

    @Query("SELECT COUNT(r) FROM PlantationRecord r WHERE r.plantedDate = :date")
    long countByPlantedDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(r) FROM PlantationRecord r WHERE r.plantedDate >= :start AND r.plantedDate <= :end")
    long countByPlantedDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /** date → planted count (for daily trend, from trendStart onwards) */
    @Query("SELECT r.plantedDate, COUNT(r) FROM PlantationRecord r " +
           "WHERE r.plantedDate >= :start GROUP BY r.plantedDate ORDER BY r.plantedDate")
    List<Object[]> countPlantedByDate(@Param("start") LocalDate start);

    /** zone → planted count */
    @Query("SELECT p.zone, COUNT(r) FROM PlantationRecord r " +
    	       "JOIN r.booking b " +
    	       "JOIN b.slot s " +
    	       "JOIN s.park p " +
    	       "JOIN b.items bi " +
    	       "JOIN bi.inventory inv " +
    	       //"JOIN p.zone z " +
    	       "GROUP BY p.zone " +
    	       "ORDER BY p.zone")
    List<Object[]> countPlantedByZone();

    /** species → planted count (top N via Pageable) */
    @Query("SELECT sp.commonName, sp.scientificName, sp.emojiCode, SUM(bi.quantity) " +
            "FROM PlantationRecord r JOIN r.booking b JOIN  b.items bi"
            //+ " JOIN  b.slot s JOIN  s.park "
            + " JOIN  bi.inventory inv JOIN  inv.species sp " +
            "GROUP BY sp.commonName, sp.scientificName, sp.emojiCode " +
            "ORDER BY COUNT(r) DESC")
	/*
	 * @Query("SELECT sp.commonName, sp.scientificName, sp.emojiCode, COUNT(r) " +
	 * "FROM PlantationRecord r JOIN r.booking b " +
	 * " JOIN  b.slot s JOIN  s.park JOIN  b.items " +
	 * " bi JOIN  bi.inventory inv JOIN  inv.species sp " +
	 * "GROUP BY sp.commonName, sp.scientificName, sp.emojiCode " +
	 * "ORDER BY COUNT(r) DESC")
	 */
    List<Object[]> countPlantedBySpecies(Pageable pageable);

    /** Most recent N plantation records with full details (limit via Pageable) */
	/*
	 * @Query("SELECT r FROM PlantationRecord r " + "JOIN FETCH r.booking b  " +
	 * "JOIN FETCH b.inventory i JOIN FETCH i.species sp JOIN FETCH i.slot s JOIN FETCH s.park p "
	 * + //"LEFT JOIN FETCH r.official o " + "ORDER BY r.recordedAt DESC")
	 * List<PlantationRecord> findRecentWithDetails(Pageable pageable);
	 */
    
    
	/*
	 * @Query(""" SELECT DISTINCT r FROM PlantationRecord r JOIN FETCH r.booking b
	 * JOIN FETCH b.slot s JOIN FETCH s.park JOIN FETCH b.items bi JOIN FETCH
	 * bi.inventory inv JOIN FETCH inv.species LEFT JOIN FETCH r.official ORDER BY
	 * r.recordedAt DESC """)
	 */
       @Query("""
       		SELECT DISTINCT r
       		FROM PlantationRecord r
       		JOIN FETCH r.booking b
       		JOIN FETCH b.slot s
       		JOIN FETCH s.park
       		JOIN FETCH b.items bi
       		JOIN FETCH bi.inventory inv
       		JOIN FETCH inv.species
       		ORDER BY r.recordedAt DESC
       		""")
    		List<PlantationRecord> findRecentWithDetails(Pageable pageable);
}
