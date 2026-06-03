package com.comne.ejib.domain.property.dto;

import java.math.BigDecimal;

public interface PropertyDetailProjection {

    String getPropertyName();

    String getPropertyAddress();

    Double getAverageTotalScore();

    Long getReviewCount();

    String getPropertyType();

    String getTransactionType();

    Integer getMonthlyRent();

    Integer getDeposit();

    Integer getFloor();

    BigDecimal getArea();

    String getAgency();

    Integer getDistanceToSchool();

    String getDescription();
}
