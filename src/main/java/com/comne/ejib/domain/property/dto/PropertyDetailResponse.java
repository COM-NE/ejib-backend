package com.comne.ejib.domain.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PropertyDetailResponse {

    private final String propertyName;

    @JsonProperty("property-address")
    private final String propertyAddress;

    private final Double averageTotalScore;
    private final Long reviewCount;
    private final String propertyType;
    private final String transactionType;
    private final Integer monthlyRent;
    private final Integer deposit;
    private final Integer floor;
    private final BigDecimal area;
    private final String agency;
    private final Integer distanceToSchool;
    private final String description;

    private PropertyDetailResponse(
            String propertyName,
            String propertyAddress,
            Double averageTotalScore,
            Long reviewCount,
            String propertyType,
            String transactionType,
            Integer monthlyRent,
            Integer deposit,
            Integer floor,
            BigDecimal area,
            String agency,
            Integer distanceToSchool,
            String description
    ) {
        this.propertyName = propertyName;
        this.propertyAddress = propertyAddress;
        this.averageTotalScore = roundToFirstDecimal(averageTotalScore);
        this.reviewCount = reviewCount == null ? 0L : reviewCount;
        this.propertyType = propertyType;
        this.transactionType = transactionType;
        this.monthlyRent = monthlyRent;
        this.deposit = deposit;
        this.floor = floor;
        this.area = area;
        this.agency = agency;
        this.distanceToSchool = distanceToSchool;
        this.description = description;
    }

    public static PropertyDetailResponse from(PropertyDetailProjection projection) {
        return new PropertyDetailResponse(
                projection.getPropertyName(),
                projection.getPropertyAddress(),
                projection.getAverageTotalScore(),
                projection.getReviewCount(),
                projection.getPropertyType(),
                projection.getTransactionType(),
                projection.getMonthlyRent(),
                projection.getDeposit(),
                projection.getFloor(),
                projection.getArea(),
                projection.getAgency(),
                projection.getDistanceToSchool(),
                projection.getDescription()
        );
    }

    private static Double roundToFirstDecimal(Double value) {
        if (value == null) {
            return 0.0;
        }

        return Math.round(value * 10) / 10.0;
    }
}
