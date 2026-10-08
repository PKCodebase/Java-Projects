package com.mcd.plantation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record LowStockAlert(
    UUID inventoryId,
    UUID slotId,
    LocalDate slotDate,
    LocalTime slotTime,
    UUID parkId,
    String parkName,
    UUID speciesId,
    String treeName,
    String treeEmoji,
    int stockQty,
    int reservedQty,
    int availableQty
) {}
