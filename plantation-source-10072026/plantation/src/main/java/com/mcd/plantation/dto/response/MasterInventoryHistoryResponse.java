package com.mcd.plantation.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MasterInventoryHistoryResponse(
    UUID historyId,
    String action,
    UUID speciesId,
    String speciesName,
    String emojiCode,
    int qty,
    UUID parkId,
    String parkName,
    String performedBy,
    OffsetDateTime performedAt
) {}
