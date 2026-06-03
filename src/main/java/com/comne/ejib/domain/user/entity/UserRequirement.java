package com.comne.ejib.domain.user.entity;

import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;

import java.util.Arrays;

public enum UserRequirement {
    WATER("water"),
    CLEANLINESS("cleanliness"),
    OPTION("option"),
    NOISE("noise"),
    LIGHTING("lighting"),
    ACCESS("access"),
    MAINTENANCE("maintenance"),
    PUBLIC_SPACE("public-space"),
    CONVENIENCE("convenience"),
    SAFETY("safety"),
    CCTV("cctv"),
    LANDLORD("landlord");

    private final String value;

    UserRequirement(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static UserRequirement from(String value) {
        return Arrays.stream(values())
                .filter(requirement -> requirement.value.equalsIgnoreCase(value == null ? "" : value.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ONBOARDING_REQUIREMENT));
    }
}
