package com.comne.ejib.domain.review.service;

import com.comne.ejib.domain.property.entity.Property;
import com.comne.ejib.domain.property.repository.PropertyRepository;
import com.comne.ejib.domain.review.entity.Review;
import com.comne.ejib.domain.review.entity.ReviewImage;
import com.comne.ejib.domain.review.repository.ReviewImageRepository;
import com.comne.ejib.domain.review.repository.ReviewRepository;
import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class ReviewPerformanceTest {

    @Autowired
    private ReviewService reviewService;

    @MockitoBean
    private com.cloudinary.Cloudinary cloudinary;

    @MockitoBean
    private com.google.cloud.vision.v1.ImageAnnotatorClient imageAnnotatorClient;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewImageRepository reviewImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    private Long targetPropertyId;

    @BeforeEach
    void setUp() {
        // 데이터 초기화
        reviewImageRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        propertyRepository.deleteAllInBatch();

        User user = userRepository.save(User.builder()
                .nickname("성능테스터")
                .profileImage(1)
                .jobType("학생")
                .point(0)
                .kakaoId("test-kakao")
                .build());

        Property property = propertyRepository.save(Property.builder()
                .address("서울시 관악구 대학동")
                .build());
        targetPropertyId = property.getId();

        // 100개의 리뷰와 각 리뷰당 3개의 이미지 생성
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            reviews.add(Review.builder()
                    .user(user)
                    .property(property)
                    .reviewType("실거주")
                    .totalScore(5)
                    .content("리뷰 내용 " + i)
                    .build());
        }
        reviewRepository.saveAll(reviews);

        List<ReviewImage> images = new ArrayList<>();
        for (Review review : reviews) {
            for (int j = 0; j < 3; j++) {
                images.add(ReviewImage.builder()
                        .review(review)
                        .imageUrl("http://image.url/" + review.getId() + "/" + j)
                        .build());
            }
        }
        reviewImageRepository.saveAll(images);
        
        System.out.println(">>> 테스트 데이터 생성 완료 (리뷰 100개, 이미지 300개)");
    }

    @Test
    @DisplayName("리뷰 목록 조회 성능 측정 (N+1 문제 확인)")
    void measureReviewListPerformance() {
        StopWatch stopWatch = new StopWatch("Review List Performance");
        
        // 1. 첫 번째 실행 (Cold Start 및 N+1 쿼리 확인용)
        System.out.println("--- 1차 실행 시작 ---");
        stopWatch.start("Get Reviews (1st)");
        reviewService.getReviewsByPropertyId(targetPropertyId);
        stopWatch.stop();
        System.out.println("--- 1차 실행 종료 ---");

        // 2. 반복 실행을 통한 평균 성능 측정
        long totalTime = 0;
        int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            reviewService.getReviewsByPropertyId(targetPropertyId);
            totalTime += (System.currentTimeMillis() - start);
        }

        System.out.println("\n========================================");
        System.out.println("1차 실행 소요 시간: " + stopWatch.getLastTaskTimeMillis() + "ms");
        System.out.println(iterations + "회 반복 평균 소요 시간: " + (totalTime / iterations) + "ms");
        System.out.println("========================================\n");
    }
}
