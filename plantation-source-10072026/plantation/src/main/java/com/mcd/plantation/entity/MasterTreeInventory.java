package com.mcd.plantation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "master_tree_inventory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MasterTreeInventory {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "master_inv_id")
    private UUID masterInvId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false, unique = true)
    private TreeSpecies species;

    @Builder.Default
    @Column(nullable = false)
    private int totalQty = 0;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
