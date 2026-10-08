package com.mcd.plantation.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ParkAssignmentResponse(
    UUID assignmentId,
    UUID parkId,
    String parkName,
    ZoneResponse parkZone,
    String officialId,
    String officialName,
    String officialEmployeeId,
    String officialRole,
    OffsetDateTime assignedAt
) {}
