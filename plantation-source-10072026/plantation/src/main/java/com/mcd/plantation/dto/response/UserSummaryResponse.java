package com.mcd.plantation.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserSummaryResponse(
    String citizenId,
    String fullName,
    String email,
    String phone,
    String aadhaarLast4,
    String address,
    boolean isActive,
    OffsetDateTime registeredAt,
    OffsetDateTime lastLoginAt
) {}
