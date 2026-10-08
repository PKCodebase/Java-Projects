package com.mcd.plantation.dto.response;

import java.util.UUID;

public record InventoryCell(
    UUID inventoryId,
    UUID speciesId,
    int stockQty,
    int reservedQty,
    int availableQty,
    String stockStatus
) {}
