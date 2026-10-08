package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateInventoryRequest(
    @Min(0) int stockQty
) {}
