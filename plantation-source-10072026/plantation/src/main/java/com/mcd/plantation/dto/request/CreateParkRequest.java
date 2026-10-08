package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateParkRequest(
    @NotBlank @Size(max = 255) String name,
    @NotNull String zoneId,
    //UUID wardId,
    String address,
    @Size(max = 100) String city,
    Double latitude,
    Double longitude,
    String description,
    Boolean isActive
) {}
