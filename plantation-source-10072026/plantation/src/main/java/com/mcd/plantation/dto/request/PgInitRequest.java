package com.mcd.plantation.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PgInitRequest (
		String     applicationCode,
	    String     applicantName,
	    String     transactionId,
	    BigDecimal amount,
	    String     returnUrl,
	    String     cancelUrl,
	    UUID     bookingId,
	    PgFinDetailRequest cnpPgFinDetailRequest
		){
}
