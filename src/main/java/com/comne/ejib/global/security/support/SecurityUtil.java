package com.comne.ejib.global.security.support;

import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public static String getCurrentPrincipalName() {
        if (!isAuthenticated()) {
            return null;
        }

        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    public static Long getCurrentUserId() {
        String name = getCurrentPrincipalName();
        if (name == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
        try {
            return Long.valueOf(name);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
    }
}