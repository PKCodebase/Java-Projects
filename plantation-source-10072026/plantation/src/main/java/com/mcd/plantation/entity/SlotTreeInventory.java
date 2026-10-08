package com.mcd.plantation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "slot_tree_inventory",
    uniqueConstraints = @UniqueConstraint(
        name = "unique_slot_species",
        columnNames = {"slot_id", "species_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SlotTreeInventory {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "inventory_id")
    private UUID inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private ParkSlot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false)
    private TreeSpecies species;

    @Column(nullable = false)
    private int stockQty = 0;

    @Column(nullable = false)
    private int reservedQty = 0;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    // ── Computed ─────────────────────────────────────────────
    @Transient
    public int getAvailableQty() { return stockQty - reservedQty; }

    @Transient
    public boolean hasAvailability() 
    { return getAvailableQty() > 0; }
    

    @Transient
    public boolean hasAvailability(int quantity) {
        return getAvailableQty() >= quantity;
    }


    // ── Mutators ─────────────────────────────────────────────
    public void reserve() {
        if (!hasAvailability())
            throw new IllegalStateException("No stock available for " + species.getCommonName());
        this.reservedQty++;
    }

    public void release() {
        if (this.reservedQty > 0) this.reservedQty--;
    }

    public void addStock(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Stock quantity must be positive");
        this.stockQty += qty;
    }
    
    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (!hasAvailability(quantity)) {
            throw new IllegalStateException(
                "Only " + getAvailableQty() + " plants available for " + species.getCommonName()
            );
        }

        this.reservedQty += quantity;
    }

    public void release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (quantity > reservedQty) {
            throw new IllegalArgumentException("Cannot release more than reserved quantity");
        }

        this.reservedQty -= quantity;
    }

    
}
