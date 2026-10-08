
package com.mcd.plantation.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingItemRequest(

		@NotNull UUID speciesId,

		@Min(1) int quantity) {
}
