package com.mcd.plantation.dto.response;

/**
 * Result of POST /bookings/{bookingId}/payment/init.
 *
 * @param url     browser redirect for the payment session the gateway created
 *                when the booking was made, or {@code null} when the gateway
 *                did not hand back something we can open in a browser.
 * @param message human-readable explanation for the UI (always safe to show —
 *                never contains a gateway payload).
 */
public record InitPaymentResponse(
		String url,
		String message
		) {
}
