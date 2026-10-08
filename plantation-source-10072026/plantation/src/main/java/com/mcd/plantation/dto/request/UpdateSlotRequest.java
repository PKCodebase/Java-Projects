package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateSlotRequest(
    @NotNull LocalDate slotDate,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    @Min(1) @Max(500) int capacity
) {}
