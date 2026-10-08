package com.mcd.plantation.entity;

import com.mcd.plantation.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_transaction_guid", 
    referencedColumnName = "payment_transaction_guid")
    private PaymentTransaction paymentTransaction;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal gstAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal feeAmount;

    /** Computed by DB: amount + gst_amount + fee_amount (GENERATED ALWAYS AS) */
    @Column(precision = 10, scale = 2, insertable = false, updatable = false)
    private BigDecimal totalAmount;

    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    private OffsetDateTime paidAt;

    @CreationTimestamp @Column(updatable = false)
    private OffsetDateTime createdAt;

}
