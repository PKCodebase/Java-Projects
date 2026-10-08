package com.mcd.plantation.repository;

import com.mcd.plantation.entity.McdOfficial;
import com.mcd.plantation.enums.OfficialRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface McdOfficialRepository extends JpaRepository<McdOfficial, String> {

    Optional<McdOfficial> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmployeeIdAndOfficialIdNot(String employeeId, String officialId);

    List<McdOfficial> findAllByIsActiveTrueOrderByName();

    List<McdOfficial> findAllByOrderByName();

    List<McdOfficial> findByRoleOrderByName(OfficialRole role);

    List<McdOfficial> findByEmployeeId(String officerSystemCode);
}
