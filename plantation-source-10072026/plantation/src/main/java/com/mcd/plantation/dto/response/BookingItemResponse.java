package com.mcd.plantation.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record BookingItemResponse(

    UUID inventoryId,

    UUID speciesId,

    String treeName,

    String treeEmoji,

    Integer quantity,

    BigDecimal unitPrice,

    BigDecimal totalPrice

) {}