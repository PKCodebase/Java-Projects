package com.mcd.plantation.dto.response;

import java.util.UUID;

public record ZoneResponse(
    String zoneId,
    String name,
    String code,
    boolean isActive
) {}
