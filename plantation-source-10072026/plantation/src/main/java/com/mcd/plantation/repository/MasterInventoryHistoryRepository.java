package com.mcd.plantation.repository;

import com.mcd.plantation.entity.MasterInventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MasterInventoryHistoryRepository extends JpaRepository<MasterInventoryHistory, UUID> {

    @Query("SELECT h FROM MasterInventoryHistory h " +
           "JOIN FETCH h.species s " +
           "LEFT JOIN FETCH h.park p " +
           //"LEFT JOIN FETCH h.performedBy o " +
           "ORDER BY h.performedAt DESC")
    List<MasterInventoryHistory> findAllOrderByPerformedAtDesc();
}
