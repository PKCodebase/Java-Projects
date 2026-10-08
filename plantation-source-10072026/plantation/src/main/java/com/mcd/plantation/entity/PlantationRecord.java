package com.mcd.plantation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "plantation_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlantationRecord {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "record_id")
    private UUID recordId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

	/*
	 * @ManyToOne(fetch = FetchType.LAZY)
	 * 
	 * @JoinColumn(name = "official_id") private McdOfficial official;
	 */
    
    @Column(name = "official_id")
    private String official;

    @Column(length = 50)
    private String treeTagId;

    @Column(length = 500)
    private String photoUrl;

    @Column(length = 100)
    private String photoBucket;

    @Column(length = 300)
    private String photoKey;

    private Double gpsLat;
    private Double gpsLng;

    @Column(nullable = false)
    private LocalDate plantedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp @Column(updatable = false)
    private OffsetDateTime recordedAt;
}
