package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.*;

public record AdminResetPasswordRequest(
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String newPassword
) {}
