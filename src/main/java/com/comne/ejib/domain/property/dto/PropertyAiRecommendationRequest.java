package com.comne.ejib.domain.property.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 매물 추천 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyAiRecommendationRequest {
    @NotBlank(message = "지역 정보는 필수입니다.")
    private String region;

    @NotBlank(message = "요구사항을 입력해주세요.")
    private String userRequest;
}
