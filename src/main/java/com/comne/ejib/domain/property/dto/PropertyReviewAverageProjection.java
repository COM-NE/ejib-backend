package com.comne.ejib.domain.property.dto;

public interface PropertyReviewAverageProjection {

    Double getAverageTotalScore();

    Double getAverageHouseScore();

    Double getAverageFacilityScore();

    Double getAverageInfraScore();

    Double getAverageSafetyScore();

    Double getAverageEnvScore();
}
