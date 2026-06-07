package com.comne.ejib.domain.auth.controller;

import com.comne.ejib.domain.auth.dto.KakaoLoginResponse;
import com.comne.ejib.domain.auth.dto.TestLoginRequest;
import com.comne.ejib.domain.auth.service.TestAuthService;
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

@Profile("dev")
@Tag(name = "테스트 인증")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/test")
public class TestAuthController {

    private final TestAuthService testAuthService;

    @Operation(summary = "테스트 로그인", description = "develop 프로필에서만 userId로 테스트용 토큰을 발급합니다.")
    @PostMapping("/tokens")
    public ResponseEntity<KakaoLoginResponse> issueTestTokens(
            @Valid @RequestBody TestLoginRequest request
    ) {
        return ResponseEntity.ok(testAuthService.issueTokens(request.userId()));
    }
}
