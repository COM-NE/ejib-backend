package com.comne.ejib.domain.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PropertyImageResponse {

    private final Long id;

    @JsonProperty("image_url")
    private final String imageUrl;

    private PropertyImageResponse(Long id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }

    public static PropertyImageResponse of(Long id, String imageUrl) {
        return new PropertyImageResponse(id, imageUrl);
    }
}
