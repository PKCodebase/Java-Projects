package com.mcd.plantation.entity;

import com.mcd.plantation.enums.BookingStatus;
import com.mcd.plantation.enums.PresenceMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_id")
    private UUID bookingId;

    @Column(nullable = false, unique = true, length = 30)
    private String bookingRef;

    @Column(name = "citizen_id")
    private String citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private ParkSlot slot;

    /** Direct link to slot+species inventory row — the authoritative source of park/tree/slot */
	/*
	 * @ManyToOne(fetch = FetchType.LAZY)
	 * 
	 * @JoinColumn(name = "inventory_id", nullable = false) private
	 * SlotTreeInventory inventory;
	 */
    
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING_PAYMENT;

    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Enumerated(EnumType.STRING)
    private PresenceMode presenceMode;

    @Column(precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occasion_id")
    private Occasion occasion;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp @Column(updatable = false)
    private OffsetDateTime bookedAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
    
    @OneToMany(mappedBy = "booking",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<BookingItems> items;

    // ── Derived helpers ────────────────────────────────────────
    @Transient
    public boolean isCancellable() {
        return status != BookingStatus.COMPLETED && status != BookingStatus.PLANTED;
    }
}
