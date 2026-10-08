package com.mcd.plantation.dto.request;


public record PgJson (
		 String orderAmount,
		 String requestType,
		 String cscId,
		 String applicationTransactionId,
		 String applicantName,
		 String applicationCode,
		 String applicationName,
		 String l_usr
		 )
{}