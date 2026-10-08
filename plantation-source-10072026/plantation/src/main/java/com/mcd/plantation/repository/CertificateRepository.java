package com.mcd.plantation.repository;

import com.mcd.plantation.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, UUID> {

    // Ek booking ke saare certificates (tree × quantity)
    @Query("SELECT c FROM Certificate c " +
            "JOIN FETCH c.booking b " +
            "JOIN FETCH b.slot s " +
            "JOIN FETCH s.park " +
            "JOIN FETCH c.bookingItem bi " +
            "JOIN FETCH bi.inventory i " +
            "JOIN FETCH i.species " +
            "LEFT JOIN FETCH c.record r " +
            "WHERE b.bookingId = :bookingId " +
            "ORDER BY c.treeIndex ASC")
    List<Certificate> findByBookingBookingId(@org.springframework.data.repository.query.Param("bookingId") UUID bookingId);

    @Query("SELECT c FROM Certificate c " +
            "JOIN FETCH c.booking b " +
            "JOIN FETCH b.slot s " +
            "JOIN FETCH s.park " +
            "JOIN FETCH c.bookingItem bi " +
            "JOIN FETCH bi.inventory i " +
            "JOIN FETCH i.species " +
            "LEFT JOIN FETCH c.record r " +
            "WHERE c.certNumber = :certNumber")
    Optional<Certificate> findByCertNumber(@org.springframework.data.repository.query.Param("certNumber") String certNumber);

    @Query("SELECT c FROM Certificate c " +
            "JOIN FETCH c.booking b " +
            "JOIN FETCH b.slot s " +
            "JOIN FETCH s.park " +
            "JOIN FETCH c.bookingItem bi " +
            "JOIN FETCH bi.inventory i " +
            "JOIN FETCH i.species " +
            "LEFT JOIN FETCH c.record r " +
            "WHERE b.bookingId IN :bookingIds " +
            "ORDER BY c.treeIndex ASC")
    List<Certificate> findByBookingBookingIdIn(@org.springframework.data.repository.query.Param("bookingIds") List<UUID> bookingIds);

    @Query("SELECT c FROM Certificate c " +
            "JOIN FETCH c.booking b " +
            "JOIN FETCH b.slot s " +
            "JOIN FETCH s.park " +
            "JOIN FETCH c.bookingItem bi " +
            "JOIN FETCH bi.inventory i " +
            "JOIN FETCH i.species " +
            "WHERE c.isValid = true ORDER BY c.issuedAt DESC")
    List<Certificate> findAllValid();
}
