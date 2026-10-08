package com.mcd.plantation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "park_tree_inventory",
    uniqueConstraints = @UniqueConstraint(columnNames = {"park_id", "species_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParkTreeInventory {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "park_inv_id")
    private UUID parkInvId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id", nullable = false)
    private Park park;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false)
    private TreeSpecies species;

    @Builder.Default
    @Column(nullable = false)
    private int allocatedQty = 0;

    @CreationTimestamp
    @Column(name = "allocated_at", nullable = false, updatable = false)
    private OffsetDateTime allocatedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
