package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RecordPlantationRequest(
    @NotNull LocalDate plantedDate,
    String treeTagId,
    Double gpsLat,
    Double gpsLng,
    String notes
) {}
