package com.mcd.plantation.dto.response;

import com.mcd.plantation.enums.TreeCategory;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TreeSpeciesResponse(
    UUID speciesId,
    String commonName,
    String scientificName,
    String emojiCode,
    BigDecimal price,
    TreeCategory category,
    String benefits,
    String careNotes,
    boolean isActive,
    List<OccasionResponse> preferredOccasions
) {}
