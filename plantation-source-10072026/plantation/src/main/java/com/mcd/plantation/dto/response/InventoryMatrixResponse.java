package com.mcd.plantation.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InventoryMatrixResponse(
    UUID parkId,
    String parkName,
    LocalDate date,
    List<String> speciesNames,
    List<SlotMatrixRow> rows
) {}
