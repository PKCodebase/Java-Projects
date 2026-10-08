package com.mcd.plantation.dto.request;

import com.mcd.plantation.enums.OfficialRole;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull OfficialRole role) {}
