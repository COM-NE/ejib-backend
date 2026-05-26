package com.comne.ejib.domain.auth.service;

import com.comne.ejib.domain.auth.dto.KakaoTokenResponse;
import com.comne.ejib.domain.auth.dto.KakaoUserResponse;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import com.comne.ejib.global.security.kakao.KakaoOAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class KakaoAuthClient {

    private static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";
    private static final String BEARER_PREFIX = "Bearer ";

    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final RestClient.Builder restClientBuilder;

    public KakaoTokenResponse requestToken(String authorizationCode) {
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", AUTHORIZATION_CODE_GRANT_TYPE);
            body.add("client_id", kakaoOAuthProperties.clientId());
            body.add("redirect_uri", kakaoOAuthProperties.redirectUri());
            body.add("code", authorizationCode);
            if (kakaoOAuthProperties.hasClientSecret()) {
                body.add("client_secret", kakaoOAuthProperties.clientSecret());
            }

            KakaoTokenResponse response = restClientBuilder.build()
                    .post()
                    .uri(kakaoOAuthProperties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(KakaoTokenResponse.class);

            if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
                throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
            }
            return response;
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new BusinessException(ErrorCode.INVALID_KAKAO_AUTHORIZATION_CODE);
            }
            throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
        }
    }

    public KakaoUserResponse requestUserInfo(String kakaoAccessToken) {
        try {
            KakaoUserResponse response = restClientBuilder.build()
                    .get()
                    .uri(kakaoOAuthProperties.userInfoUri())
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + kakaoAccessToken)
                    .retrieve()
                    .body(KakaoUserResponse.class);

            if (response == null || response.id() == null) {
                throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
            }
            return response;
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new BusinessException(ErrorCode.INVALID_KAKAO_TOKEN);
            }
            throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
        }
    }
}
