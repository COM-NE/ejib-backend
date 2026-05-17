package com.comne.ejib.domain.auth.dto;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
    public static AuthTokenResponse bearer(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn,
            long refreshTokenExpiresIn
    ) {
        return new AuthTokenResponse(
                "Bearer",
                accessToken,
                refreshToken,
                accessTokenExpiresIn,
                refreshTokenExpiresIn
        );
    }
}
