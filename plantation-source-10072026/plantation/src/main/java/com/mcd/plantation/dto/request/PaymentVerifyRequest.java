package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PaymentVerifyRequest(
    @NotBlank String payResponse
) {}
