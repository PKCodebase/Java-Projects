package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AllocateToParkRequest(
    @NotNull UUID speciesId,
    @Min(1) int qty
) {}
