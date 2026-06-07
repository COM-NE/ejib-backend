package com.comne.ejib.domain.property.dto;

import lombok.Getter;

@Getter
public class PropertySearchResponse {

    private final Long id;
    private final String propertyName;
    private final String propertyAddress;

    private final Double averageTotalScore;
    private final Long reviewCount;
    private final String transactionType;

    private PropertySearchResponse(
            Long id,
            String propertyName,
            String propertyAddress,
            Double averageTotalScore,
            Long reviewCount,
            String transactionType
    ) {
        this.id = id;
        this.propertyName = propertyName;
        this.propertyAddress = propertyAddress;
        this.averageTotalScore = roundToFirstDecimal(averageTotalScore);
        this.reviewCount = reviewCount == null ? 0L : reviewCount;
        this.transactionType = transactionType;
    }

    public static PropertySearchResponse from(PropertySearchProjection projection) {
        return new PropertySearchResponse(
                projection.getId(),
                projection.getPropertyName(),
                projection.getPropertyAddress(),
                projection.getAverageTotalScore(),
                projection.getReviewCount(),
                projection.getTransactionType()
        );
    }

    private static Double roundToFirstDecimal(Double value) {
        if (value == null) {
            return 0.0;
        }

        return Math.round(value * 10) / 10.0;
    }
}
