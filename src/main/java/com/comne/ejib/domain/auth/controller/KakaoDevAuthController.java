package com.comne.ejib.domain.auth.controller;

import com.comne.ejib.domain.auth.dto.AuthTokenResponse;
import com.comne.ejib.domain.auth.dto.KakaoLoginResponse;
import com.comne.ejib.domain.auth.dto.KakaoLoginTicketRequest;
import com.comne.ejib.domain.auth.service.KakaoLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "local-kakao-auth-test", description = "로컬 개발용 카카오 로그인 토큰 교환 API")
@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dev/auth/kakao")
public class KakaoDevAuthController {

    private final KakaoLoginService kakaoLoginService;

    @Operation(
            summary = "카카오 로그인 티켓으로 JWT 토큰 발급 - 로컬 개발용",
            description = """
                    로컬 개발 편의를 위한 테스트 API입니다.
                    브라우저에서 /oauth/kakao/authorize 로그인 후 프론트 성공 URL에 포함된 ticket 값을 요청 본문으로 전달하면
                    accessToken, refreshToken을 반환합니다.
                    이 API는 dev 프로필에서만 활성화됩니다.
                    """
    )
    @PostMapping("/tokens")
    public ResponseEntity<AuthTokenResponse> exchangeLoginTicketForTokens(
            @Valid @RequestBody KakaoLoginTicketRequest request
    ) {
        KakaoLoginResponse response = kakaoLoginService.exchangeTicket(request.ticket());
        return ResponseEntity.ok(new AuthTokenResponse(response.accessToken(), response.refreshToken()));
    }
}
