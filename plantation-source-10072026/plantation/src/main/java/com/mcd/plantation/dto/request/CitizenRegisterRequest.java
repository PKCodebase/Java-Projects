package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.*;

public record CitizenRegisterRequest(
    @NotBlank String fullName,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number") String phone,
    @Size(min = 4, max = 4) String aadhaarLast4,
    String address
) {}
