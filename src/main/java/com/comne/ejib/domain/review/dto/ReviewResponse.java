package com.comne.ejib.domain.review.dto;

import com.comne.ejib.domain.review.entity.Review;
import com.comne.ejib.domain.review.entity.ReviewImage;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record ReviewResponse(
        Long id,
        Long userId,
        Long propertyId,
        String reviewType,
        Integer residenceDuration,
        Integer totalScore,
        Integer houseScore,
        Integer facilityScore,
        Integer infraScore,
        Integer safetyScore,
        Integer envScore,
        String content,
        Integer deposit,
        Integer monthlyRent,
        List<String> imageUrls,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .propertyId(review.getProperty().getId())
                .reviewType(review.getReviewType())
                .residenceDuration(review.getResidenceDuration())
                .totalScore(review.getTotalScore())
                .houseScore(review.getHouseScore())
                .facilityScore(review.getFacilityScore())
                .infraScore(review.getInfraScore())
                .safetyScore(review.getSafetyScore())
                .envScore(review.getEnvScore())
                .content(review.getContent())
                .deposit(review.getDeposit())
                .monthlyRent(review.getMonthlyRent())
                .imageUrls(review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .collect(Collectors.toList()))
                .createdAt(review.getCreatedAt())
                .build();
    }
}
