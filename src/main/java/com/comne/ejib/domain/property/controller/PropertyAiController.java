package com.comne.ejib.domain.property.controller;

import com.comne.ejib.domain.property.dto.PropertyAiRecommendationRequest;
import com.comne.ejib.domain.property.dto.PropertyAiRecommendationResponse;
import com.comne.ejib.domain.property.service.PropertyAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * AI 기반 매물 추천 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class PropertyAiController {

    private final PropertyAiService propertyAiService;

    /**
     * AI 기반 매물 추천 API
     * 사용자의 요구사항과 지역을 기반으로 최적의 매물 하나를 추천합니다.
     * 비동기로 처리되어 Gemini API의 응답을 기다리는 동안 메인 스레드를 점유하지 않습니다.
     *
     * @param request 지역 정보 및 요구사항
     * @return 추천 결과 (propertyId, 이유, 적합도 점수)
     */
    @PostMapping("/recommend")
    public CompletableFuture<ResponseEntity<PropertyAiRecommendationResponse>> recommendProperty(
            @RequestBody @Valid PropertyAiRecommendationRequest request) {
        
        return propertyAiService.getRecommendation(request.getUserRequest(), request.getRegion())
                .thenApply(response -> {
                    if (response == null) {
                        return ResponseEntity.noContent().build();
                    }
                    return ResponseEntity.ok(response);
                });
    }
}
