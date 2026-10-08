package com.mcd.plantation.dto.response;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UserSummary user
) {
    public static AuthResponse of(String access, String refresh, long exp, UserSummary user) {
        return new AuthResponse(access, refresh, "Bearer", exp, user);
    }
}
