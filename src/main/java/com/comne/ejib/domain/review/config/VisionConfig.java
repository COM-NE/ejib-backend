package com.comne.ejib.domain.review.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class VisionConfig {
    @Value("${google.cloud.vision.credentials.location}")
    private Resource credentialsLocation;

    @Bean
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        try (InputStream is = credentialsLocation.getInputStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsLocation.getInputStream());
            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
            return ImageAnnotatorClient.create(settings);
        }
    }
}
