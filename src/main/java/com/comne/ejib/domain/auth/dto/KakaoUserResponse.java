package com.comne.ejib.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(
        Long id,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    public String nickname() {
        if (kakaoAccount == null || kakaoAccount.profile == null) {
            return null;
        }
        return kakaoAccount.profile.nickname;
    }

    public record KakaoAccount(
            Profile profile
    ) {
    }

    public record Profile(
            String nickname
    ) {
    }
}
