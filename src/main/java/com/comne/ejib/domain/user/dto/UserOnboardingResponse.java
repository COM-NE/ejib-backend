package com.comne.ejib.domain.user.dto;

public record UserOnboardingResponse(
        boolean onboardingCompleted
) {
    public static UserOnboardingResponse completed() {
        return new UserOnboardingResponse(true);
    }
}
