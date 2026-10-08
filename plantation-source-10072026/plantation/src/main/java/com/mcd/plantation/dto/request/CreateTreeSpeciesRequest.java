package com.mcd.plantation.dto.request;

import com.mcd.plantation.enums.TreeCategory;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateTreeSpeciesRequest(
    @NotBlank String commonName,
    String scientificName,
    String emojiCode,
    @NotNull @DecimalMin("0.01") BigDecimal price,
    @NotNull TreeCategory category,
    String benefits,
    String careNotes,
    Boolean isActive,
    List<UUID> preferredOccasionIds
) {}
