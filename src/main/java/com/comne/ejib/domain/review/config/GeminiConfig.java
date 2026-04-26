package com.comne.ejib.domain.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Gemini API 통신을 위한 RestTemplate 설정 클래스
 */
@Configuration
public class GeminiConfig {

    @Bean
    public RestTemplate geminiRestTemplate() {
        return new RestTemplate();
    }
}
