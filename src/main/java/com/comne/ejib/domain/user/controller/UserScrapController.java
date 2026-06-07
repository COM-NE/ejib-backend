package com.comne.ejib.domain.user.controller;

import com.comne.ejib.domain.property.dto.PropertySearchResponse;
import com.comne.ejib.domain.property.service.PropertyService;
import com.comne.ejib.global.security.support.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "사용자 스크랩", description = "로그인한 사용자의 스크랩 매물 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/scraps")
public class UserScrapController {

    private final PropertyService propertyService;

    @Operation(summary = "내 스크랩 매물 목록 조회", description = "로그인한 사용자가 스크랩한 모든 매물을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<PropertySearchResponse>> getMyScrappedProperties() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(propertyService.getScrappedProperties(userId));
    }
}
