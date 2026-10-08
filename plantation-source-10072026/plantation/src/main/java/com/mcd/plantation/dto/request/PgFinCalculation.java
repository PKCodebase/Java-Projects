package com.mcd.plantation.dto.request;


public record PgFinCalculation(
		 String financialYear,
	     String collectionPeriod,
	     String periodFrom,
	     String periodTo,
	     String totalAmount,
	     String finalAmount,
	     String ownershipCategory,
	     String ownershipType,
	     PgFinComponents components
		)
{
}