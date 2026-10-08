package com.mcd.plantation.dto.request;

import com.mcd.plantation.enums.PresenceMode;
import jakarta.validation.constraints.NotNull;

public record SetPresenceModeRequest(
    @NotNull PresenceMode presenceMode
) {}
