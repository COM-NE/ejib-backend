package com.comne.ejib.domain.user.entity;

import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;

import java.util.Arrays;

public enum UserStatus {
    STUDENT("student"),
    JOB_SEEKER("job-seeker"),
    WORKER("worker"),
    ETC("etc");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static UserStatus from(String value) {
        return Arrays.stream(values())
                .filter(status -> status.value.equalsIgnoreCase(value == null ? "" : value.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ONBOARDING_STATUS));
    }
}
