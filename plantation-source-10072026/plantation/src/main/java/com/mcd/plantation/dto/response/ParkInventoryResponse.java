package com.mcd.plantation.dto.response;

import com.mcd.plantation.enums.TreeCategory;
import java.util.UUID;

public record ParkInventoryResponse(
    UUID parkInvId,
    UUID parkId,
    String parkName,
    UUID speciesId,
    String commonName,
    String scientificName,
    String emojiCode,
    TreeCategory category,
    int allocatedQty,
    int usedQty,
    int bookedQty,
    int availableQty
) {}
