package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Body of {@code POST /auth/refresh}. */
public record RefreshRequest(
    @NotBlank String refreshToken
) {}
