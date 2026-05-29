package com.comne.ejib.config;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("test")
public class TestGoogleVisionConfig {

    @Bean
    public ImageAnnotatorClient imageAnnotatorClient() {
        return mock(ImageAnnotatorClient.class);
    }
}