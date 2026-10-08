package com.mcd.plantation.dto.response;

import java.util.UUID;

public record OccasionResponse(
    UUID occasionId,
    String name,
    String description,
    String emojiCode,
    int displayOrder,
    boolean isActive
) {}
