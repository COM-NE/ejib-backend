package com.comne.ejib.domain.property.controller;

import com.comne.ejib.domain.property.dto.PropertyDetailResponse;
import com.comne.ejib.domain.property.dto.PropertyImageResponse;
import com.comne.ejib.domain.property.dto.PropertyReviewsResponse;
import com.comne.ejib.domain.property.dto.PropertySearchResponse;
import com.comne.ejib.domain.property.service.PropertyService;
import com.comne.ejib.global.security.support.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "매물", description = "매물 조회 API")
@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;


    @Operation(summary = "매물 이름 검색", description = "매물명에 검색어가 포함된 매물 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<PropertySearchResponse>> searchProperties(@RequestParam String name) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(propertyService.searchPropertiesByName(userId, name));
    }

    @Operation(summary = "매물 상세 조회", description = "매물 기본 정보와 리뷰 통계를 조회합니다.")
    @GetMapping("/{propertyId}")
    public ResponseEntity<PropertyDetailResponse> getPropertyDetail(@PathVariable Long propertyId) {
        return ResponseEntity.ok(propertyService.getPropertyDetail(propertyId));
    }

    @Operation(summary = "매물 리뷰 이미지 목록 조회", description = "매물에 연결된 모든 리뷰 이미지를 조회합니다.")
    @GetMapping("/{propertyId}/images")
    public ResponseEntity<List<PropertyImageResponse>> getPropertyImages(@PathVariable Long propertyId) {
        return ResponseEntity.ok(propertyService.getPropertyImages(propertyId));
    }

    @Operation(summary = "매물 리뷰 목록 조회", description = "매물에 연결된 모든 리뷰의 점수 평균과 리뷰 목록을 조회합니다.")
    @GetMapping("/{propertyId}/reviews")
    public ResponseEntity<PropertyReviewsResponse> getPropertyReviews(@PathVariable Long propertyId) {
        return ResponseEntity.ok(propertyService.getPropertyReviews(propertyId));
    }
}
