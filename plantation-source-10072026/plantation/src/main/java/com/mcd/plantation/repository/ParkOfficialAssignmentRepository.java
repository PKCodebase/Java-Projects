package com.mcd.plantation.repository;

import com.mcd.plantation.entity.ParkOfficialAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParkOfficialAssignmentRepository extends JpaRepository<ParkOfficialAssignment, UUID> {

    List<ParkOfficialAssignment> findByPark_ParkId(UUID parkId);

    List<ParkOfficialAssignment> findByOfficial(String officialId);

    boolean existsByPark_ParkIdAndOfficial(UUID parkId, String officialId);

    Optional<ParkOfficialAssignment> findByPark_ParkIdAndOfficial(UUID parkId, String officialId);

    @Query("SELECT a.park.parkId FROM ParkOfficialAssignment a WHERE a.official = :officialId")
    List<UUID> findParkIdsByOfficialId(@Param("officialId") String officialId);
}
