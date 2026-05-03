package com.comne.ejib.domain.review.service;

import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Gemini API와의 통신을 담당하는 클라이언트 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiClient {

    @Value("${google.gemini.api-key}")
    private String apiKey;

    @Value("${google.gemini.url:https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash-lite:generateContent}")
    private String apiUrl;

    private final RestTemplate geminiRestTemplate;

    /**
     * 프롬프트를 Gemini API에 전달하고 응답을 받습니다.
     * 응답 형식을 JSON으로 강제합니다.
     *
     * @param prompt AI에게 전달할 프롬프트
     * @return AI가 생성한 응답 문자열 (JSON 형식)
     */
    public String generateContent(String prompt) {
        String url = apiUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        // API 요청 바디 구성 (v1beta 기준 responseMimeType 사용)
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generation_config", Map.of(  // generationConfig -> generation_config (권장)
                        "responseMimeType", "application/json" // responseMimeType -> response_mime_type (필수)
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Gemini API 호출 시작 (Model: 2.5-flash-lite)...");
            // RestTemplate이 에러 응답 본문을 보여주지 않을 때를 대비해 구체적인 로그를 남깁니다.
            Map<String, Object> response = geminiRestTemplate.postForObject(url, entity, Map.class);
            return extractTextFromResponse(response);
        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생: {}", e.getMessage());
            // 팁: 여기서 ErrorCode.AI_API_ERROR 같은 전용 에러를 쓰면 더 좋습니다.
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gemini API 응답 구조에서 실제 텍스트 내용을 추출합니다.
     */
    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        if (response == null || !response.containsKey("candidates")) {
            log.error("Gemini API 응답 구조가 올바르지 않습니다.");
            return "{}";
        }

        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates.isEmpty()) return "{}";

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts.isEmpty()) return "{}";

            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            log.error("응답 데이터 파싱 실패: {}", e.getMessage());
            return "{}";
        }
    }
}
