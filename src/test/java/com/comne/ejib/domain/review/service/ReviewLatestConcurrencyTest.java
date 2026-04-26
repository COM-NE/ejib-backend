package com.comne.ejib.domain.review.service;

import com.comne.ejib.domain.property.entity.Property;
import com.comne.ejib.domain.property.repository.PropertyRepository;
import com.comne.ejib.domain.review.entity.Review;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@ActiveProfiles("test")
public class ReviewLatestConcurrencyTest {

    @Autowired
    private ReviewService reviewService;

    @MockitoBean
    private com.cloudinary.Cloudinary cloudinary;

    @MockitoBean
    private com.google.cloud.vision.v1.ImageAnnotatorClient imageAnnotatorClient;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        propertyRepository.deleteAllInBatch();

        User user = userRepository.save(User.builder()
                .nickname("부하테스터")
                .profileImage(1)
                .jobType("학생")
                .point(0)
                .kakaoId("load-test-kakao")
                .build());

        Property property = propertyRepository.save(Property.builder()
                .address("서울시")
                .build());

        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            reviews.add(Review.builder()
                    .user(user)
                    .property(property)
                    .content("리뷰 " + i)
                    .build());
        }
        reviewRepository.saveAll(reviews);
        System.out.println(">>> 테스트 데이터 1,000건 생성 완료");
    }

    @Test
    @DisplayName("100명의 사용자가 동시에 최신 리뷰를 조회할 때의 안정성 테스트")
    void measureConcurrentAccessPerformance() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    reviewService.getLatestReviews();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("요청 실패: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long endTime = System.currentTimeMillis();

        System.out.println("\n========================================");
        System.out.println("동시 접속자 수: " + threadCount);
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());
        System.out.println("전체 소요 시간: " + (endTime - startTime) + "ms");
        System.out.println("인당 평균 응답 시간: " + (double)(endTime - startTime) / threadCount + "ms");
        System.out.println("========================================\n");
        
        executorService.shutdown();
    }
}
