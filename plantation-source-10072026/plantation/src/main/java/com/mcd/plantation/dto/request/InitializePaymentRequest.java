package com.mcd.plantation.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record InitializePaymentRequest(
			@NotNull UUID bookingId
		) 
{}
