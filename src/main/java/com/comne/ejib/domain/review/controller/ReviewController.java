package com.comne.ejib.domain.review.controller;

import com.comne.ejib.domain.review.dto.ReviewRequest;
import com.comne.ejib.domain.review.dto.ReviewResponse;
import com.comne.ejib.domain.review.service.ReviewService;
import com.comne.ejib.global.security.support.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @RequestPart("request") @Valid ReviewRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {

        Long userId = SecurityUtil.getCurrentUserId();
        ReviewResponse response = reviewService.createReview(userId, request, images);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 매물(Property)에 등록된 모든 리뷰를 조회합니다.
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByPropertyId(@PathVariable Long propertyId) {
        return ResponseEntity.ok(reviewService.getReviewsByPropertyId(propertyId));
    }

    /**
     * 특정 유저(User)가 작성한 모든 리뷰를 조회합니다.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUserId(userId));
    }

    /**
     * 가장 최근에 등록된 리뷰 3개를 조회합니다.
     */
    @GetMapping("/latest")
    public ResponseEntity<List<ReviewResponse>> getLatestReviews() {
        return ResponseEntity.ok(reviewService.getLatestReviews());
    }
}
