package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateParkInventoryRequest(
    @Min(0) int qty
) {}
