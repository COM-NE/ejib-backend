package com.comne.ejib.domain.property.service;

import com.comne.ejib.domain.property.dto.PropertyAiRecommendationResponse;
import com.comne.ejib.domain.property.entity.Property;
import com.comne.ejib.domain.property.repository.PropertyRepository;
import com.comne.ejib.domain.review.entity.Review;
import com.comne.ejib.domain.review.service.GeminiClient;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI 기반 매물 추천 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyAiService {

    private final PropertyRepository propertyRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    /**
     * 사용자의 요구사항과 지역 정보를 바탕으로 최적의 매물을 추천합니다.
     * 비동기로 실행되어 응답 지연을 방지합니다.
     *
     * @param userRequest 사용자의 요구사항 (예: "조용하고 치안 좋은 곳")
     * @param region 조회 지역 (예: "서울시 동작구")
     * @return 추천 결과 (CompletableFuture)
     */
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<PropertyAiRecommendationResponse> getRecommendation(String userRequest, String region) {
        log.info("AI 매물 추천 시작: 지역={}, 요구사항={}", region, userRequest);

        // 와일드카드 이스케이프 처리 (SQL Injection 방지)
        String escapedRegion = region.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        // 1. 데이터 조회 (FETCH JOIN으로 N+1 방지)
        List<Property> properties = propertyRepository.findAllWithReviewsByRegion(escapedRegion);
        
        if (properties.isEmpty()) {
            log.warn("해당 지역에 매물이 없습니다: {}", region);
            return CompletableFuture.completedFuture(null);
        }

        // 2. AI 컨텍스트 구축 (텍스트 슬라이싱 포함)
        String context = buildAiContext(properties);

        // 3. 프롬프트 생성
        String prompt = buildPrompt(userRequest, context);

        // 4. AI 호출 및 결과 파싱
        return CompletableFuture.supplyAsync(() -> {
            try {
                String aiResponse = geminiClient.generateContent(prompt);
                // AI로부터 임시 응답(ID와 요약문)을 받음
                AiInternalResponse internalResponse = objectMapper.readValue(aiResponse, AiInternalResponse.class);
                
                // 5. 원본 데이터와 결합하여 최종 응답 생성
                return properties.stream()
                        .filter(p -> p.getId().equals(internalResponse.getPropertyId()))
                        .findFirst()
                        .map(p -> buildFinalResponse(p, internalResponse.getAiSummary()))
                        .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
            } catch (Exception e) {
                log.error("AI 추천 결과 처리 중 오류 발생: {}", e.getMessage());
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * 원본 매물 엔티티와 AI 요약문을 결합하여 최종 DTO를 생성합니다.
     */
    private PropertyAiRecommendationResponse buildFinalResponse(Property property, String aiSummary) {
        double avgScore = property.getReviews().stream()
                .mapToInt(Review::getTotalScore)
                .average()
                .orElse(0.0);

        return PropertyAiRecommendationResponse.builder()
                .propertyId(property.getId())
                .address(property.getAddress())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .averageScore(Math.round(avgScore * 10) / 10.0) // 소수점 첫째자리까지
                .aiSummary(aiSummary)
                .build();
    }

    /**
     * AI 응답을 임시로 담기 위한 내부 클래스
     */
    @Getter
    @NoArgsConstructor
    private static class AiInternalResponse {
        private Long propertyId;
        private String aiSummary;
    }

    /**
     * 매물 및 리뷰 데이터를 AI가 이해하기 쉬운 텍스트 구조로 변환합니다.
     * 토큰 제한을 위해 리뷰 개수와 글자 수를 제한(슬라이싱)합니다.
     */
    private String buildAiContext(List<Property> properties) {
        StringBuilder sb = new StringBuilder();
        for (Property p : properties) {
            sb.append(String.format("### 매물 ID: %d\n", p.getId()));
            sb.append(String.format("- 주소: %s\n", p.getAddress()));
            sb.append(String.format("- 금액: 보증금 %d / 월세 %d\n", p.getDeposit(), p.getMonthlyRent()));
            sb.append(String.format("- 설명: %s\n", p.getDescription()));
            
            List<Review> reviews = p.getReviews();
            if (!reviews.isEmpty()) {
                sb.append("- 실거주자 리뷰 요약:\n");
                // 최대 3개의 리뷰만 포함
                for (int i = 0; i < Math.min(reviews.size(), 3); i++) {
                    Review r = reviews.get(i);
                    // 리뷰 본문 150자 슬라이싱
                    String content = r.getContent();
                    if (content.length() > 150) {
                        content = content.substring(0, 150) + "...";
                    }
                    sb.append(String.format("  * (평점 %d) %s\n", r.getTotalScore(), content));
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Gemini에게 보낼 프롬프트를 생성합니다.
     */
    private String buildPrompt(String userRequest, String context) {
        return String.format("""
            당신은 부동산 전문가입니다. 아래 제공된 '매물 데이터'를 분석하여 '사용자의 요구사항'에 가장 적합한 매물 하나를 추천하고 그 이유를 요약하세요.
            
            [사용자의 요구사항]
            %s
            
            [매물 데이터]
            %s
            
            [주의 사항]
            1. 반드시 제공된 데이터 중에서만 선택하세요.
            2. 사용자의 요구사항(치안, 수압, 소음, 인프라 등)과 리뷰 내용을 매칭하여 가장 논리적인 선택을 하세요.
            3. 응답은 반드시 아래 JSON 형식을 지켜야 하며, 다른 텍스트는 포함하지 마세요.
            
            {
              "propertyId": 매물ID(Long),
              "aiSummary": "추천 이유 및 매물 리뷰 요약 (사용자의 요구사항과 매칭하여 상세히 기술)"
            }
            """, userRequest, context);
    }
}
