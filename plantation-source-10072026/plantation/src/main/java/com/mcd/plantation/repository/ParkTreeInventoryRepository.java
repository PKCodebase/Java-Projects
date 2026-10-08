package com.mcd.plantation.repository;

import com.mcd.plantation.entity.ParkTreeInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParkTreeInventoryRepository extends JpaRepository<ParkTreeInventory, UUID> {

    @Query("SELECT p FROM ParkTreeInventory p JOIN FETCH p.species s JOIN FETCH p.park pk " +
           "WHERE pk.parkId = :parkId ORDER BY s.commonName")
    List<ParkTreeInventory> findByParkIdWithSpecies(@Param("parkId") UUID parkId);

    Optional<ParkTreeInventory> findByPark_ParkIdAndSpecies_SpeciesId(UUID parkId, UUID speciesId);

    @Query("SELECT COALESCE(SUM(p.allocatedQty), 0) FROM ParkTreeInventory p " +
           "WHERE p.species.speciesId = :speciesId")
    int sumAllocatedBySpecies(@Param("speciesId") UUID speciesId);
}
