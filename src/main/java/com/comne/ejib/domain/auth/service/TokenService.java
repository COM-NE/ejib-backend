package com.comne.ejib.domain.auth.service;

import com.comne.ejib.domain.auth.dto.AuthTokenResponse;
import com.comne.ejib.domain.auth.entity.RefreshToken;
import com.comne.ejib.domain.auth.repository.RefreshTokenRepository;
import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.domain.user.repository.UserRepository;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import com.comne.ejib.global.security.jwt.JwtProperties;
import com.comne.ejib.global.security.jwt.JwtRefreshTokenClaims;
import com.comne.ejib.global.security.jwt.JwtTokenProvider;
import com.comne.ejib.global.security.jwt.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final TokenHashUtil tokenHashUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public AuthTokenResponse issueTokenPair(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshTokenId = UUID.randomUUID().toString();
        String refreshToken = jwtTokenProvider.createRefreshToken(user, refreshTokenId);

        RefreshToken savedRefreshToken = RefreshToken.builder()
                .user(user)
                .tokenId(refreshTokenId)
                .tokenHash(tokenHashUtil.sha256(refreshToken))
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenExpirationSeconds()))
                .build();
        refreshTokenRepository.save(savedRefreshToken);

        return AuthTokenResponse.of(accessToken, refreshToken);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public AuthTokenResponse rotateRefreshToken(String rawRefreshToken) {
        JwtRefreshTokenClaims claims = jwtTokenProvider.parseRefreshToken(rawRefreshToken);
        LocalDateTime now = LocalDateTime.now();

        RefreshToken refreshToken = refreshTokenRepository.findByTokenIdForUpdate(claims.tokenId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (!refreshToken.getUser().getId().equals(claims.userId())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        if (!refreshToken.getTokenHash().equals(tokenHashUtil.sha256(rawRefreshToken))) {
            revokeAllRefreshTokens(claims.userId());
            throw new BusinessException(ErrorCode.TOKEN_REUSE_DETECTED);
        }
        if (refreshToken.getRevokedAt() != null || refreshToken.getRotatedAt() != null) {
            revokeAllRefreshTokens(claims.userId());
            throw new BusinessException(ErrorCode.TOKEN_REUSE_DETECTED);
        }
        if (refreshToken.isExpired(now) || !claims.expiresAt().isAfter(now.atZone(ZoneId.systemDefault()).toInstant())) {
            refreshToken.revoke(now);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        User user = userRepository.findById(claims.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        refreshToken.rotate(now);
        return issueTokenPair(user);
    }

    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        JwtRefreshTokenClaims claims = jwtTokenProvider.parseRefreshToken(rawRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenIdForUpdate(claims.tokenId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (!refreshToken.getTokenHash().equals(tokenHashUtil.sha256(rawRefreshToken))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        refreshToken.revoke(LocalDateTime.now());
    }

    @Transactional
    public void revokeAllRefreshTokens(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(userId)
                .forEach(refreshToken -> refreshToken.revoke(now));
    }
}
