package com.mcd.plantation.dto.response;

import com.mcd.plantation.enums.SlotStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record ParkSlotResponse(
    UUID slotId,
    UUID parkId,
    String parkName,
    LocalDate slotDate,
    LocalTime startTime,
    LocalTime endTime,
    int capacity,
    int bookedCount,
    int freeSpots,
    SlotStatus status,
    List<SlotInventoryItem> inventory
) {}
