package com.comne.ejib.global.security.jwt;

import java.time.Instant;

public record JwtRefreshTokenClaims(
        Long userId,
        String tokenId,
        Instant expiresAt
) {
}
