package com.mcd.plantation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "parks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Park {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "park_id")
    private UUID parkId;

    @Column(nullable = false)
    private String name;

    @Column(name = "zone_id")
    private String zone;

	/*
	 * @ManyToOne(fetch = FetchType.LAZY)
	 * 
	 * @JoinColumn(name = "ward_id") private Ward ward;
	 */

    private String address;

    @Column(nullable = false)
    private String city = "Delhi";

    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "park", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ParkSlot> slots = new ArrayList<>();
}
