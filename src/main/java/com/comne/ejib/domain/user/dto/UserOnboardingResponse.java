package com.comne.ejib.domain.user.dto;

import java.util.List;

public record UserOnboardingResponse(
        Long userId,
        String nickname,
        String profile,
        String status,
        List<String> requirement,
        boolean onboardingCompleted
) {
}
