package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SetMasterStockRequest(
    @NotNull UUID speciesId,
    @Min(0) int qty
) {}
