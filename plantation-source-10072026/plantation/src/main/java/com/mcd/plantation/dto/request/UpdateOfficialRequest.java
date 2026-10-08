package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.*;

public record UpdateOfficialRequest(
    @NotBlank @Size(max = 255) String name,
    @Size(max = 50)            String employeeId,
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number") String phone
) {}
