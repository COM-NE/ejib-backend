package com.comne.ejib.domain.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TestLoginRequest(
        @NotNull(message = "userId는 필수입니다.")
        @Positive
        Long userId
) {
}
