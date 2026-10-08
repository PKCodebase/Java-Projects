package com.mcd.plantation.dto.request;

import com.mcd.plantation.enums.OfficialRole;
import jakarta.validation.constraints.*;
import java.util.UUID;

public record CreateOfficialRequest(
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(max = 50)  String employeeId,
    @NotBlank @Email           String email,
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number") String phone,
    @NotNull                   OfficialRole role,
    String zoneId,
    //UUID wardId,
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password
) {}
