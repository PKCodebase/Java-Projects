package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignOfficialRequest(@NotNull String officialId) {}
