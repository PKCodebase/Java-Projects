package com.mcd.plantation.repository;

import com.mcd.plantation.entity.Park;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ParkRepository extends JpaRepository<Park, UUID> {
    List<Park> findByIsActiveTrueOrderByName();
    List<Park> findByZoneAndIsActiveTrueOrderByName(String zoneId);
    List<Park> findAllByOrderByName();
    List<Park> findByZoneOrderByName(String zoneId);
    
    //List<Park> findByZone_ZoneIdAndWard_WardIdOrderByName(UUID zoneId, UUID wardId);
    List<Park> findByParkIdInOrderByName(List<UUID> parkIds);

    // Pageable variants for the public browse endpoint
    Page<Park> findByIsActiveTrueOrderByName(Pageable pageable);
    Page<Park> findByZoneAndIsActiveTrueOrderByName(String zoneId, Pageable pageable);

    // Pageable variants for the admin park management (with optional name search)
    Page<Park> findAllByOrderByName(Pageable pageable);
    Page<Park> findByNameContainingIgnoreCaseOrderByName(String name, Pageable pageable);
    Page<Park> findByZoneOrderByName(String zoneId, Pageable pageable);
    Page<Park> findByZoneAndNameContainingIgnoreCaseOrderByName(String zoneId, String name, Pageable pageable);

    long countByIsActiveTrue();
}
