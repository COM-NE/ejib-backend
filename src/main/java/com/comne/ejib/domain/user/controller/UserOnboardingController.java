package com.comne.ejib.domain.user.controller;

import com.comne.ejib.domain.user.dto.UserOnboardingRequest;
import com.comne.ejib.domain.user.dto.UserOnboardingResponse;
import com.comne.ejib.domain.user.service.UserOnboardingService;
import com.comne.ejib.global.security.support.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 온보딩")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
public class UserOnboardingController {

    private final UserOnboardingService userOnboardingService;

    @Operation(
            summary = "내 온보딩 정보 저장",
            description = "카카오 최초 로그인 사용자의 nickname, profile, status, requirement 3개를 저장합니다."
    )
    @PatchMapping("/onboarding")
    public ResponseEntity<UserOnboardingResponse> completeOnboarding(
            @Valid @RequestBody UserOnboardingRequest request
    ) {
        UserOnboardingResponse response = userOnboardingService.complete(SecurityUtil.getCurrentUserId(), request);
        return ResponseEntity.ok(response);
    }
}
