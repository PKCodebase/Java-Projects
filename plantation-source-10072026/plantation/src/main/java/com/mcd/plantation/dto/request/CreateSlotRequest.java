package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateSlotRequest(
    @NotNull UUID parkId,
    @NotNull LocalDate slotDate,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    @Min(1) @Max(500) int capacity
) {}
