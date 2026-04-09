package com.comne.ejib.domain.review.controller;

import com.comne.ejib.domain.review.dto.ReviewRequest;
import com.comne.ejib.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Long> createReview(
            @RequestPart("request") ReviewRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {

        Long reviewId = reviewService.createReview(request, images);
        return ResponseEntity.ok(reviewId);
    }
}
