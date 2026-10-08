package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOccasionRequest(
    @NotBlank String name,
    String description,
    @NotBlank String emojiCode,
    @NotNull Integer displayOrder,
    Boolean isActive
) {}
