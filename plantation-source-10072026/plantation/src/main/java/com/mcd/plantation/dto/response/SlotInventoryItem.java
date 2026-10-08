package com.mcd.plantation.dto.response;

import com.mcd.plantation.enums.TreeCategory;
import java.math.BigDecimal;
import java.util.UUID;

public record SlotInventoryItem(
    UUID inventoryId,
    UUID speciesId,
    String commonName,
    String scientificName,
    String emojiCode,
    TreeCategory category,
    BigDecimal price,
    int stockQty,
    int reservedQty,
    int availableQty,
    String stockStatus
) {
    public static String stockStatus(int available, int threshold) {
        if (available == 0) return "OUT";
        if (available <= threshold) return "LOW";
        return "AVAILABLE";
    }
}
