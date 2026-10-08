package com.mcd.plantation.dto.response;

import com.mcd.plantation.enums.OfficialRole;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OfficialDetailResponse(
    String officialId,
    String name,
    String employeeId,
    String email,
    String phone,
    OfficialRole role,
    ZoneResponse zone,
    boolean isActive,
    OffsetDateTime createdAt
) {}
