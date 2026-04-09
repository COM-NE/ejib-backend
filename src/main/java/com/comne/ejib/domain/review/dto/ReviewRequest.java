package com.comne.ejib.domain.review.dto;

public record ReviewRequest(
        Long userId,
        Long propertyId,
        String reviewType, // 후기 유형(임장/실거주)
        Integer residenceDuration, // 거주 기간
        Integer totalScore, // 총 점수
        Integer houseScore, // 집 상태
        Integer facilityScore, // 시설물
        Integer infraScore, // 인프라
        Integer safetyScore, // 치안
        Integer envScore, // 환경
        String content, // 리뷰 내용
        Integer deposit, // 후기 보증금
        Integer monthlyRent // 후기 월세
) {}