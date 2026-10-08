package com.mcd.plantation.dto.response;

import com.mcd.plantation.enums.TreeCategory;
import java.math.BigDecimal;
import java.util.UUID;

public record MasterInventoryResponse(
    UUID masterInvId,
    UUID speciesId,
    String commonName,
    String scientificName,
    String emojiCode,
    TreeCategory category,
    BigDecimal price,
    int totalQty,
    int allocatedQty,
    int availableQty
) {}
