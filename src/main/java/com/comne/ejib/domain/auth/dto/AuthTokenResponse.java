package com.comne.ejib.domain.auth.dto;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken
) {
    public static AuthTokenResponse of(String accessToken, String refreshToken) {
        return new AuthTokenResponse(accessToken, refreshToken);
    }
}
