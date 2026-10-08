package com.mcd.plantation.dto.response;

public record UserSummary(
    String id,
    String fullName,
    String email,
    String role
) {}
