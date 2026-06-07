package com.comne.ejib.domain.user.dto;

import com.comne.ejib.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserMyPageResponse {

    private final String profile;
    private final String nickname;
    private final Integer point;

    private UserMyPageResponse(String profile, String nickname, Integer point) {
        this.profile = profile;
        this.nickname = nickname;
        this.point = point;
    }

    public static UserMyPageResponse from(User user) {
        return new UserMyPageResponse(
                user.getProfile(),
                user.getNickname(),
                user.getPoint()
        );
    }
}
