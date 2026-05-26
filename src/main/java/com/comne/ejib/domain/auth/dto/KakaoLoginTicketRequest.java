package com.comne.ejib.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginTicketRequest(
        @NotBlank(message = "로그인 티켓은 필수입니다.")
        String ticket
) {
}
