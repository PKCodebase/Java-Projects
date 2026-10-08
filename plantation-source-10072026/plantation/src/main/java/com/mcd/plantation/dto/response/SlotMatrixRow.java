package com.mcd.plantation.dto.response;

import com.mcd.plantation.enums.SlotStatus;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record SlotMatrixRow(
    UUID slotId,
    LocalTime startTime,
    LocalTime endTime,
    int capacity,
    int bookedCount,
    int freeSpots,
    SlotStatus status,
    List<InventoryCell> cells
) {}
