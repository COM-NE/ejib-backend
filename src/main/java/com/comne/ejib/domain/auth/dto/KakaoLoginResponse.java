package com.comne.ejib.domain.auth.dto;

public record KakaoLoginResponse(
        String accessToken,
        String refreshToken,
        boolean newUser,
        boolean onboardingCompleted
) {
    public static KakaoLoginResponse of(
            AuthTokenResponse tokens,
            boolean newUser,
            boolean onboardingCompleted
    ) {
        return new KakaoLoginResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                newUser,
                onboardingCompleted
        );
    }
}
