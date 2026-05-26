package com.comne.ejib.global.security.jwt;

import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "typ";
    private static final String SCOPE_CLAIM = "scope";
    private static final String TOKEN_ID_CLAIM = "jti";
    private static final String USER_SCOPE = "USER";
    private static final MacAlgorithm JWT_SIGNING_ALGORITHM = MacAlgorithm.HS256;

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.accessTokenExpirationSeconds());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(String.valueOf(user.getId()))
                .claim(TOKEN_TYPE_CLAIM, JwtTokenType.ACCESS.value())
                .claim(SCOPE_CLAIM, USER_SCOPE)
                .claim("nickname", user.getNickname())
                .build();

        JwsHeader header = JwsHeader.with(JWT_SIGNING_ALGORITHM).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String createRefreshToken(User user, String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.refreshTokenExpirationSeconds());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(String.valueOf(user.getId()))
                .id(tokenId)
                .claim(TOKEN_TYPE_CLAIM, JwtTokenType.REFRESH.value())
                .build();

        JwsHeader header = JwsHeader.with(JWT_SIGNING_ALGORITHM).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public JwtRefreshTokenClaims parseRefreshToken(String refreshToken) {
        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);
            String tokenType = jwt.getClaimAsString(TOKEN_TYPE_CLAIM);
            if (!JwtTokenType.REFRESH.value().equals(tokenType)) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }
            String tokenId = jwt.getId();
            Instant expiresAt = jwt.getExpiresAt();
            if (tokenId == null || tokenId.isBlank() || expiresAt == null) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }

            return new JwtRefreshTokenClaims(
                    Long.valueOf(jwt.getSubject()),
                    tokenId,
                    expiresAt
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
