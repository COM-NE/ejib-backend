package com.comne.ejib.domain.auth.service;

import com.comne.ejib.domain.auth.dto.AuthTokenResponse;
import com.comne.ejib.domain.auth.dto.KakaoLoginResponse;
import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.domain.user.repository.UserRepository;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Profile("dev")
@Service
@RequiredArgsConstructor
public class TestAuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Transactional
    public KakaoLoginResponse issueTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        AuthTokenResponse tokens = tokenService.issueTokenPair(user);
        return KakaoLoginResponse.of(tokens, false, user.isOnboardingCompleted());
    }
}
