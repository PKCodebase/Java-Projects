
package com.mcd.plantation.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "booking_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItems {

	@Id

	@GeneratedValue(strategy = GenerationType.UUID)

	@Column(name = "booking_item_id")
	private UUID bookingItemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_id", nullable = false, unique = true)
	private Booking booking;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "inventory_id", nullable = false)
	private SlotTreeInventory inventory;

	@Column(name = "quantity")
	private Integer quantity;

	@Column(name = "unit_price", precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "total_price", precision = 10, scale = 2)
	private BigDecimal totalPrice;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private OffsetDateTime createdAt;

}
