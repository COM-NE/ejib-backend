package com.comne.ejib.domain.property.dto;

import lombok.Getter;

@Getter
public class PropertyScrapResponse {

    private final boolean scrapped;

    private PropertyScrapResponse(boolean scrapped) {
        this.scrapped = scrapped;
    }

    public static PropertyScrapResponse of(boolean scrapped) {
        return new PropertyScrapResponse(scrapped);
    }
}
