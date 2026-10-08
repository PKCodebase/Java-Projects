package com.mcd.plantation.repository;

import com.mcd.plantation.entity.MasterTreeInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MasterTreeInventoryRepository extends JpaRepository<MasterTreeInventory, UUID> {

    Optional<MasterTreeInventory> findBySpecies_SpeciesId(UUID speciesId);

    @Query("SELECT m FROM MasterTreeInventory m JOIN FETCH m.species s ORDER BY s.commonName")
    List<MasterTreeInventory> findAllWithSpecies();
}
