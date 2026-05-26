package com.comne.ejib.domain.auth.service;

import com.comne.ejib.domain.auth.dto.AuthTokenResponse;
import com.comne.ejib.domain.auth.dto.KakaoLoginResponse;
import com.comne.ejib.domain.auth.dto.KakaoTokenResponse;
import com.comne.ejib.domain.auth.dto.KakaoUserResponse;
import com.comne.ejib.domain.auth.entity.KakaoLoginTicket;
import com.comne.ejib.domain.auth.repository.KakaoLoginTicketRepository;
import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.domain.user.repository.UserRepository;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import com.comne.ejib.global.security.jwt.TokenHashUtil;
import com.comne.ejib.global.security.kakao.KakaoOAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KakaoLoginService {

    private static final int DEFAULT_PROFILE_IMAGE = 0;
    private static final String DEFAULT_JOB_TYPE = "UNKNOWN";
    private static final int DEFAULT_POINT = 0;
    private static final int TICKET_BYTE_LENGTH = 32;

    private final KakaoAuthClient kakaoAuthClient;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final KakaoLoginTicketRepository kakaoLoginTicketRepository;
    private final TokenHashUtil tokenHashUtil;
    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String loginAndCreateTicket(String authorizationCode) {
        KakaoTokenResponse kakaoToken = kakaoAuthClient.requestToken(authorizationCode);
        KakaoUserResponse kakaoUser = kakaoAuthClient.requestUserInfo(kakaoToken.accessToken());

        String kakaoId = String.valueOf(kakaoUser.id());
        UserLookupResult lookupResult = findOrCreateUser(kakaoId, kakaoUser.nickname());
        AuthTokenResponse tokens = tokenService.issueTokenPair(lookupResult.user());

        String ticket = createSecureTicket();
        KakaoLoginTicket loginTicket = KakaoLoginTicket.builder()
                .ticketHash(tokenHashUtil.sha256(ticket))
                .user(lookupResult.user())
                .newUser(lookupResult.newUser())
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .accessTokenExpiresIn(tokens.accessTokenExpiresIn())
                .refreshTokenExpiresIn(tokens.refreshTokenExpiresIn())
                .expiresAt(LocalDateTime.now().plusSeconds(kakaoOAuthProperties.resolvedLoginTicketExpirationSeconds()))
                .build();
        kakaoLoginTicketRepository.save(loginTicket);

        return ticket;
    }

    @Transactional
    public KakaoLoginResponse exchangeTicket(String rawTicket) {
        LocalDateTime now = LocalDateTime.now();
        KakaoLoginTicket loginTicket = kakaoLoginTicketRepository.findByTicketHashForUpdate(tokenHashUtil.sha256(rawTicket))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_TICKET));

        if (loginTicket.isUsed() || loginTicket.isExpired(now)) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_TICKET);
        }

        loginTicket.use(now);
        User user = loginTicket.getUser();
        AuthTokenResponse tokens = AuthTokenResponse.bearer(
                loginTicket.getAccessToken(),
                loginTicket.getRefreshToken(),
                loginTicket.getAccessTokenExpiresIn(),
                loginTicket.getRefreshTokenExpiresIn()
        );
        return new KakaoLoginResponse(user.getId(), user.getNickname(), loginTicket.isNewUser(), tokens);
    }

    private UserLookupResult findOrCreateUser(String kakaoId, String kakaoNickname) {
        return userRepository.findByKakaoId(kakaoId)
                .map(user -> new UserLookupResult(user, false))
                .orElseGet(() -> new UserLookupResult(createUser(kakaoId, kakaoNickname), true));
    }

    private User createUser(String kakaoId, String kakaoNickname) {
        String nickname = createUniqueNickname(kakaoNickname, kakaoId);
        User user = User.builder()
                .kakaoId(kakaoId)
                .nickname(nickname)
                .profileImage(DEFAULT_PROFILE_IMAGE)
                .jobType(DEFAULT_JOB_TYPE)
                .point(DEFAULT_POINT)
                .build();
        return userRepository.save(user);
    }

    private String createUniqueNickname(String kakaoNickname, String kakaoId) {
        String baseNickname = normalizeNickname(kakaoNickname);
        if (!userRepository.existsByNickname(baseNickname)) {
            return baseNickname;
        }

        String suffix = kakaoId.length() > 6 ? kakaoId.substring(kakaoId.length() - 6) : kakaoId;
        String candidate = trimToMaxLength(baseNickname, 13) + "_" + suffix;
        if (!userRepository.existsByNickname(candidate)) {
            return candidate;
        }

        for (int index = 1; index <= 99; index++) {
            String numberedCandidate = trimToMaxLength(baseNickname, 17) + "_" + index;
            if (!userRepository.existsByNickname(numberedCandidate)) {
                return numberedCandidate;
            }
        }
        throw new BusinessException(ErrorCode.DUPLICATED_NICKNAME);
    }

    private String normalizeNickname(String nickname) {
        String normalized = Objects.requireNonNullElse(nickname, "카카오사용자").trim();
        if (normalized.isBlank()) {
            normalized = "카카오사용자";
        }
        return trimToMaxLength(normalized, 20);
    }

    private String trimToMaxLength(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String createSecureTicket() {
        byte[] bytes = new byte[TICKET_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private record UserLookupResult(User user, boolean newUser) {
    }
}
