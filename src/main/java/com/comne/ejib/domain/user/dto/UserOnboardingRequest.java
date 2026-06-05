package com.comne.ejib.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserOnboardingRequest(
        @NotBlank
        @Size(max = 10)
        String name,

        @NotBlank
        @Size(max = 20)
        String nickname,

        @NotBlank
        String profile,

        @NotBlank
        String status,

        @NotEmpty
        @Size(min = 3, max = 3)
        List<@NotBlank String> requirement
) {
}
