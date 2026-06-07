package com.comne.ejib.domain.property.dto;

public interface PropertySearchProjection {

    Long getId();

    String getPropertyName();

    String getPropertyAddress();

    Double getAverageTotalScore();

    Long getReviewCount();

    String getTransactionType();
}
