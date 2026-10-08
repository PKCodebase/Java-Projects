package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record AddInventoryRequest(
    @NotNull UUID slotId,
    @NotNull UUID speciesId,
    @Min(0) int stockQty
) {}
