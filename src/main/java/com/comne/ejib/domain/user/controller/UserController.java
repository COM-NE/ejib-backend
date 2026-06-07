package com.comne.ejib.domain.user.controller;

import com.comne.ejib.domain.user.dto.UserMyPageResponse;
import com.comne.ejib.domain.user.service.UserMyPageService;
import com.comne.ejib.global.security.support.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자", description = "로그인한 사용자 정보 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserMyPageService userMyPageService;

    @Operation(summary = "마이페이지 조회", description = "로그인한 사용자의 프로필, 닉네임, 포인트를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserMyPageResponse> getMyPage() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(userMyPageService.getMyPage(userId));
    }
}
