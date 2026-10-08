package com.mcd.plantation.dto.response;

import com.mcd.plantation.enums.OfficialRole;
import java.util.UUID;

public record OfficialSummaryResponse(
    String officialId,
    String name,
    String employeeId,
    OfficialRole role,
    ZoneResponse zone,
    //WardResponse ward,
    boolean isActive
) {}
