package com.comne.ejib.domain.property.dto;

import java.time.LocalDateTime;

public interface PropertyReviewItemProjection {

    String getNickname();

    Integer getResidenceDuration();

    Integer getTotalScore();

    Integer getHouseScore();

    Integer getFacilityScore();

    Integer getInfraScore();

    Integer getSafetyScore();

    Integer getEnvScore();

    String getContent();

    LocalDateTime getCreatedAt();
}
