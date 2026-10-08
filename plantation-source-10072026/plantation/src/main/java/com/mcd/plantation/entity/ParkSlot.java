package com.mcd.plantation.entity;

import com.mcd.plantation.enums.SlotStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "park_slots",
    uniqueConstraints = @UniqueConstraint(columnNames = {"park_id", "slot_date", "start_time"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParkSlot {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "slot_id")
    private UUID slotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id", nullable = false)
    private Park park;

    @Column(nullable = false)
    private LocalDate slotDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Builder.Default
    @Column(nullable = false)
    private int capacity = 4;

    @Builder.Default
    @Column(nullable = false)
    private int bookedCount = 0;

    @Builder.Default
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status = SlotStatus.AVAILABLE;

    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp @Column(updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "slot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SlotTreeInventory> inventory = new ArrayList<>();

    public int getFreeSpots() { return capacity - bookedCount; }
    public boolean isFull()    { return bookedCount >= capacity; }

    public void incrementBooked() {
        this.bookedCount++;
        if (this.bookedCount >= this.capacity) this.status = SlotStatus.FULL;
    }

    public void decrementBooked() {
        if (this.bookedCount > 0) this.bookedCount--;
        if (this.status == SlotStatus.FULL) this.status = SlotStatus.AVAILABLE;
    }
}
