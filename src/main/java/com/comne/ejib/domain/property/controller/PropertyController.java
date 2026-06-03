package com.comne.ejib.domain.property.controller;

import com.comne.ejib.domain.property.dto.PropertyDetailResponse;
import com.comne.ejib.domain.property.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "매물", description = "매물 조회 API")
@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @Operation(summary = "매물 상세 조회", description = "매물 기본 정보와 리뷰 통계를 조회합니다.")
    @GetMapping("/{propertyId}")
    public ResponseEntity<PropertyDetailResponse> getPropertyDetail(@PathVariable Long propertyId) {
        return ResponseEntity.ok(propertyService.getPropertyDetail(propertyId));
    }
}
