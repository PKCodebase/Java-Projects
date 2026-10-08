package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.*;

public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}
