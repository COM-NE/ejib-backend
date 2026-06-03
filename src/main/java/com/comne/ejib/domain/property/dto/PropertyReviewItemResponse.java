package com.comne.ejib.domain.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PropertyReviewItemResponse {

    private final String nickname;
    private final Integer residenceDuration;
    private final Integer totalScore;
    private final Integer houseScore;
    private final Integer facilityScore;
    private final Integer infraScore;
    private final Integer safetyScore;
    private final Integer envScore;
    private final String content;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    private PropertyReviewItemResponse(
            String nickname,
            Integer residenceDuration,
            Integer totalScore,
            Integer houseScore,
            Integer facilityScore,
            Integer infraScore,
            Integer safetyScore,
            Integer envScore,
            String content,
            LocalDateTime createdAt
    ) {
        this.nickname = nickname;
        this.residenceDuration = residenceDuration;
        this.totalScore = totalScore;
        this.houseScore = houseScore;
        this.facilityScore = facilityScore;
        this.infraScore = infraScore;
        this.safetyScore = safetyScore;
        this.envScore = envScore;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static PropertyReviewItemResponse from(PropertyReviewItemProjection projection) {
        return new PropertyReviewItemResponse(
                projection.getNickname(),
                projection.getResidenceDuration(),
                projection.getTotalScore(),
                projection.getHouseScore(),
                projection.getFacilityScore(),
                projection.getInfraScore(),
                projection.getSafetyScore(),
                projection.getEnvScore(),
                projection.getContent(),
                projection.getCreatedAt()
        );
    }
}
