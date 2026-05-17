package com.comne.ejib.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long accessTokenExpirationSeconds,
        long refreshTokenExpirationSeconds
) {

    private static final int HS256_MIN_SECRET_BYTES = 32;

    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("jwt.issuer must not be blank");
        }
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret must not be blank");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < HS256_MIN_SECRET_BYTES) {
            throw new IllegalArgumentException("jwt.secret must be at least 32 bytes for HS256");
        }
        if (accessTokenExpirationSeconds <= 0) {
            throw new IllegalArgumentException("jwt.access-token-expiration-seconds must be > 0");
        }
        if (refreshTokenExpirationSeconds <= 0) {
            throw new IllegalArgumentException("jwt.refresh-token-expiration-seconds must be > 0");
        }
    }
}
