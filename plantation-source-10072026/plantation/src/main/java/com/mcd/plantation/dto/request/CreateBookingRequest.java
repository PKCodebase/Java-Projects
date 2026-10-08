package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record CreateBookingRequest(
    @NotNull UUID slotId,
    @NotBlank String bookingDate,
    UUID occasionId,
    @NotEmpty
    List<BookingItemRequest> items
) {}
