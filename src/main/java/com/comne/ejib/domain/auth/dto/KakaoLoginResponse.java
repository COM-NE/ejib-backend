package com.comne.ejib.domain.auth.dto;

public record KakaoLoginResponse(
        Long userId,
        String nickname,
        boolean newUser,
        boolean onboardingCompleted,
        AuthTokenResponse tokens
) {
}
