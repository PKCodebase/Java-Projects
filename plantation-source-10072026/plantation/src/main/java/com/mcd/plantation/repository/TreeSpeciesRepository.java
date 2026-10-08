package com.mcd.plantation.repository;

import com.mcd.plantation.entity.TreeSpecies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface TreeSpeciesRepository extends JpaRepository<TreeSpecies, UUID> {
    List<TreeSpecies> findByIsActiveTrueOrderByCommonName();
    List<TreeSpecies> findAllByOrderByCommonName();

    @Query("SELECT s FROM TreeSpecies s JOIN s.preferredOccasions o WHERE o.occasionId = :occasionId AND s.isActive = true ORDER BY s.commonName")
    List<TreeSpecies> findActiveByOccasionId(@Param("occasionId") UUID occasionId);
}
