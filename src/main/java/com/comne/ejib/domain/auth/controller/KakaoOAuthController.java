package com.comne.ejib.domain.auth.controller;

import com.comne.ejib.domain.auth.dto.KakaoLoginResponse;
import com.comne.ejib.domain.auth.dto.KakaoLoginTicketRequest;
import com.comne.ejib.domain.auth.service.KakaoLoginService;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import com.comne.ejib.global.security.kakao.KakaoOAuthProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Base64;

@Tag(name = "카카오 인증")
@RestController
@RequiredArgsConstructor
@RequestMapping
public class KakaoOAuthController {

    private static final String STATE_COOKIE_NAME = "KAKAO_OAUTH_STATE";
    private static final int STATE_BYTE_LENGTH = 32;

    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final KakaoLoginService kakaoLoginService;
    private final SecureRandom secureRandom = new SecureRandom();

    @GetMapping("/oauth/kakao/authorize")
    public ResponseEntity<Void> authorize() {
        String state = createSecureState();
        URI kakaoAuthorizeUri = UriComponentsBuilder.fromUriString(kakaoOAuthProperties.authorizeUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", kakaoOAuthProperties.clientId())
                .queryParam("redirect_uri", kakaoOAuthProperties.redirectUri())
                .queryParam("state", state)
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(kakaoAuthorizeUri);
        headers.add(HttpHeaders.SET_COOKIE, createStateCookie(state).toString());
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @CookieValue(name = STATE_COOKIE_NAME, required = false) String savedState
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, deleteStateCookie().toString());

        try {
            validateCallback(code, state, savedState, error);
            String ticket = kakaoLoginService.loginAndCreateTicket(code);
            headers.setLocation(successUri(ticket));
        } catch (BusinessException e) {
            headers.setLocation(failureUri(e.getErrorCode()));
        }

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @PostMapping("/api/v1/auth/kakao/tokens")
    public ResponseEntity<KakaoLoginResponse> exchangeLoginTicket(
            @Valid @RequestBody KakaoLoginTicketRequest request
    ) {
        KakaoLoginResponse response = kakaoLoginService.exchangeTicket(request.ticket());
        return ResponseEntity.ok(response);
    }

    private void validateCallback(String code, String state, String savedState, String error) {
        if (StringUtils.hasText(error)) {
            throw new BusinessException(ErrorCode.KAKAO_LOGIN_FAILED);
        }
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(ErrorCode.INVALID_KAKAO_AUTHORIZATION_CODE);
        }
        if (!StringUtils.hasText(state) || !StringUtils.hasText(savedState) || !state.equals(savedState)) {
            throw new BusinessException(ErrorCode.INVALID_KAKAO_OAUTH_STATE);
        }
    }

    private URI successUri(String ticket) {
        return UriComponentsBuilder.fromUriString(kakaoOAuthProperties.frontendSuccessUri())
                .queryParam("ticket", ticket)
                .build()
                .encode()
                .toUri();
    }

    private URI failureUri(ErrorCode errorCode) {
        return UriComponentsBuilder.fromUriString(kakaoOAuthProperties.frontendFailureUri())
                .queryParam("error", errorCode.getCode())
                .build()
                .encode()
                .toUri();
    }

    private ResponseCookie createStateCookie(String state) {
        return ResponseCookie.from(STATE_COOKIE_NAME, state)
                .httpOnly(true)
                .secure(kakaoOAuthProperties.stateCookieSecure())
                .sameSite("Lax")
                .path("/oauth/kakao")
                .maxAge(kakaoOAuthProperties.resolvedStateExpirationSeconds())
                .build();
    }

    private ResponseCookie deleteStateCookie() {
        return ResponseCookie.from(STATE_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(kakaoOAuthProperties.stateCookieSecure())
                .sameSite("Lax")
                .path("/oauth/kakao")
                .maxAge(0)
                .build();
    }

    private String createSecureState() {
        byte[] bytes = new byte[STATE_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
