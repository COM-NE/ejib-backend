package com.comne.ejib.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OcrVerifyResponse {
    private boolean isVerified;
    private String message;
}