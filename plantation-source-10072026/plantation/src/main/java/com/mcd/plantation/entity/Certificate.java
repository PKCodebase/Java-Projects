package com.mcd.plantation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "certificates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Certificate {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cert_id")
    private UUID certId;

    // Many certificates per booking (one per tree × quantity)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Which booking item (tree species) this certificate belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_item_id", nullable = false)
    private BookingItems bookingItem;

    // Index within the quantity (1-based). e.g. Neem x3 → treeIndex 1,2,3
    @Column(nullable = false)
    private int treeIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private PlantationRecord record;

    @Column(nullable = false, unique = true, length = 60)
    private String certNumber;      // MCD/CERT/2025/000001

    @Column(length = 500)
    private String pdfUrl;

    @Column(length = 100)
    private String pdfBucket;

    @Column(length = 300)
    private String pdfKey;

    @Column(nullable = false)
    @Builder.Default
    private boolean isValid = true;

    private OffsetDateTime revokedAt;

    @Column(columnDefinition = "TEXT")
    private String revokeNote;

    @CreationTimestamp @Column(updatable = false)
    private OffsetDateTime issuedAt;
}
