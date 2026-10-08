package com.mcd.plantation.dto.response;

import java.util.UUID;

public record ParkResponse(
    UUID parkId,
    String name,
    ZoneResponse zone,
    String address,
    String city,
    Double latitude,
    Double longitude,
    String description,
    boolean isActive,
    int totalSlots,
    int bookedSlots,
    int freeSlots
) {}
