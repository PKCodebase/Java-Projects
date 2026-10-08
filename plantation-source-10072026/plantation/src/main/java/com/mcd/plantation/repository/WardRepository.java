/*
 * package com.mcd.plantation.repository;
 * 
 * import com.mcd.plantation.entity.Ward; import
 * org.springframework.data.jpa.repository.JpaRepository; import
 * org.springframework.stereotype.Repository; import java.util.List; import
 * java.util.UUID;
 * 
 * @Repository public interface WardRepository extends JpaRepository<Ward, UUID>
 * { List<Ward> findByZone_ZoneIdAndIsActiveTrueOrderByName(UUID zoneId);
 * List<Ward> findByZone_ZoneIdOrderByName(UUID zoneId); boolean
 * existsByZone_ZoneIdAndName(UUID zoneId, String name); }
 */