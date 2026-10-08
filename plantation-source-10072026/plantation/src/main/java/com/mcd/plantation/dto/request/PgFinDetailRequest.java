package com.mcd.plantation.dto.request;

import java.util.List;

public record PgFinDetailRequest(
		 String applicationCode,
	     String applicationTransactionId,
	     String paymentTransactionId,
	     String orderAmount,
	     String transactionStatus,
	     String transactionPurpose,
	     String transactionInitiatedOn,
	     String transactionModifiedOn,
	     String paymentAggregrator,
	     String zoneCode,
	     String uref,
	     List<PgFinCalculation> calculationJson
		) {
}