package com.mcd.plantation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "master_inventory_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MasterInventoryHistory {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id")
    private UUID historyId;

    @Column(nullable = false, length = 30)
    private String action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false)
    private TreeSpecies species;

    @Column(nullable = false)
    private int qty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id")
    private Park park;

	/*
	 * @ManyToOne(fetch = FetchType.LAZY)
	 * 
	 * @JoinColumn(name = "performed_by")
	 */
    @Column(name="performed_by")
    private String performedBy;

    @CreationTimestamp
    @Column(name = "performed_at", nullable = false, updatable = false)
    private OffsetDateTime performedAt;
}
