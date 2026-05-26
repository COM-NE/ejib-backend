package com.comne.ejib.global.security.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.oauth")
public record KakaoOAuthProperties(
        String clientId,
        String clientSecret,
        String authorizeUri,
        String tokenUri,
        String userInfoUri,
        String redirectUri,
        String frontendSuccessUri,
        String frontendFailureUri,
        boolean stateCookieSecure,
        long stateExpirationSeconds,
        long loginTicketExpirationSeconds
) {
    public boolean hasClientSecret() {
        return clientSecret != null && !clientSecret.isBlank();
    }

    public long resolvedStateExpirationSeconds() {
        return stateExpirationSeconds > 0 ? stateExpirationSeconds : 300L;
    }

    public long resolvedLoginTicketExpirationSeconds() {
        return loginTicketExpirationSeconds > 0 ? loginTicketExpirationSeconds : 180L;
    }
}
