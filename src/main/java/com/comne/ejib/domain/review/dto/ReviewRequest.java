package com.comne.ejib.domain.review.dto;

import jakarta.validation.constraints.*;

public record ReviewRequest(
        @NotNull(message = "매물 ID는 필수입니다.")
        Long propertyId,

        @NotBlank(message = "후기 유형을 선택해주세요. (임장/실거주)")
        String reviewType,

        @Min(value = 0, message = "거주 기간은 0개월 이상이어야 합니다.")
        Integer residenceDuration,

        @NotNull(message = "종합 만족도를 선택해주세요.")
        @Min(1) @Max(5)
        Integer totalScore,

        @Min(1) @Max(5)
        Integer houseScore,

        @Min(1) @Max(5)
        Integer facilityScore,

        @Min(1) @Max(5)
        Integer infraScore,

        @Min(1) @Max(5)
        Integer safetyScore,

        @Min(1) @Max(5)
        Integer envScore,

        @NotBlank(message = "리뷰 내용을 입력해주세요.")
        @Size(min = 10, max = 1000, message = "리뷰 내용은 10자 이상 1000자 이하로 작성해주세요.")
        String content,

        @Min(value = 0, message = "보증금은 0원 이상이어야 합니다.")
        Integer deposit,

        @Min(value = 0, message = "월세는 0원 이상이어야 합니다.")
        Integer monthlyRent
) {}