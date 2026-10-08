package com.mcd.plantation.repository;

import com.mcd.plantation.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, UUID> {

    Optional<Citizen> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Citizen> findAllByOrderByFullName();
}
