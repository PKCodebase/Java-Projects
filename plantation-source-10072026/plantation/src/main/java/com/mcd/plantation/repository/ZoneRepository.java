/*
 * package com.mcd.plantation.repository;
 * 
 * import com.mcd.plantation.entity.Zone; import
 * org.springframework.data.jpa.repository.JpaRepository; import
 * org.springframework.stereotype.Repository; import java.util.List; import
 * java.util.Optional; import java.util.UUID;
 * 
 * @Repository public interface ZoneRepository extends JpaRepository<Zone, UUID>
 * { List<Zone> findByIsActiveTrueOrderByName(); List<Zone>
 * findAllByOrderByName(); Optional<Zone> findByName(String name); boolean
 * existsByName(String name); }
 */