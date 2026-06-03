package com.comne.ejib.domain.user.entity;

import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;

import java.util.Arrays;

public enum UserProfile {
    BLUE("blue", 0),
    RED("red", 1),
    YELLOW("yellow", 2);

    private final String value;
    private final int code;

    UserProfile(String value, int code) {
        this.value = value;
        this.code = code;
    }

    public String value() {
        return value;
    }

    public int code() {
        return code;
    }

    public static UserProfile from(String value) {
        return Arrays.stream(values())
                .filter(profile -> profile.value.equalsIgnoreCase(value == null ? "" : value.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ONBOARDING_PROFILE));
    }

    public static UserProfile fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(profile -> profile.code == code)
                .findFirst()
                .orElse(null);
    }
}
