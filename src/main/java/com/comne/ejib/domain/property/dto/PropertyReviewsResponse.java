package com.comne.ejib.domain.property.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PropertyReviewsResponse {

    private final Double averageTotalScore;
    private final Double averageHouseScore;
    private final Double averageFacilityScore;
    private final Double averageInfraScore;
    private final Double averageSafetyScore;
    private final Double averageEnvScore;
    private final List<PropertyReviewItemResponse> reviews;

    private PropertyReviewsResponse(
            Double averageTotalScore,
            Double averageHouseScore,
            Double averageFacilityScore,
            Double averageInfraScore,
            Double averageSafetyScore,
            Double averageEnvScore,
            List<PropertyReviewItemResponse> reviews
    ) {
        this.averageTotalScore = roundToFirstDecimal(averageTotalScore);
        this.averageHouseScore = roundToFirstDecimal(averageHouseScore);
        this.averageFacilityScore = roundToFirstDecimal(averageFacilityScore);
        this.averageInfraScore = roundToFirstDecimal(averageInfraScore);
        this.averageSafetyScore = roundToFirstDecimal(averageSafetyScore);
        this.averageEnvScore = roundToFirstDecimal(averageEnvScore);
        this.reviews = List.copyOf(reviews);
    }

    public static PropertyReviewsResponse of(
            PropertyReviewAverageProjection averages,
            List<PropertyReviewItemResponse> reviews
    ) {
        return new PropertyReviewsResponse(
                averages.getAverageTotalScore(),
                averages.getAverageHouseScore(),
                averages.getAverageFacilityScore(),
                averages.getAverageInfraScore(),
                averages.getAverageSafetyScore(),
                averages.getAverageEnvScore(),
                reviews
        );
    }

    private static Double roundToFirstDecimal(Double value) {
        if (value == null) {
            return 0.0;
        }

        return Math.round(value * 10) / 10.0;
    }
}
