package com.mcd.plantation.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mcd.plantation.entity.PaymentTransaction;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String>{

	List<PaymentTransaction> findByPaymentTransactionNumber(String paymentTransactionNumber);

	/** Newest first — used to hand a pending booking its gateway payment URL. */
	List<PaymentTransaction> findByBookingBookingIdOrderByCreatedDateDesc(UUID bookingId);
}
