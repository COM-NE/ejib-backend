package com.comne.ejib.domain.property.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * AI 매물 추천 및 요약 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyAiRecommendationResponse {
    private Long propertyId;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double averageScore;
    private String aiSummary;
}
