package com.comne.ejib.domain.review.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.comne.ejib.domain.property.entity.Property;
import com.comne.ejib.domain.property.repository.PropertyRepository;
import com.comne.ejib.domain.review.dto.ReviewRequest;
import com.comne.ejib.domain.review.dto.ReviewResponse;
import com.comne.ejib.domain.review.repository.ReviewImageRepository;
import com.comne.ejib.domain.review.repository.ReviewRepository;
import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class ReviewServiceIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewImageRepository reviewImageRepository;

    @MockitoBean
    private com.google.cloud.vision.v1.ImageAnnotatorClient imageAnnotatorClient;

    @MockitoBean
    private Cloudinary cloudinary;

    private User testUser;
    private Property testProperty;

    @BeforeEach
    void setUp() throws IOException {
        reviewImageRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        propertyRepository.deleteAllInBatch();
        
        // User 엔티티의 nullable = false 필드들을 채워서 저장
        testUser = userRepository.save(User.builder()
                .nickname("테스터_" + System.currentTimeMillis())
                .profileImage(1)
                .jobType("학생")
                .point(0)
                .kakaoId("kakao_" + System.currentTimeMillis())
                .build());

        testProperty = propertyRepository.save(Property.builder()
                .address("서울시 강남구")
                .build());

        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(ArgumentMatchers.any(byte[].class), anyMap())).thenAnswer(invocation -> {
            Thread.sleep(1000); // 1초 지연
            Map<String, Object> result = new HashMap<>();
            result.put("secure_url", "https://image.url/" + System.nanoTime());
            return result;
        });
    }

    @Test
    @DisplayName("이미지 5장을 포함한 리뷰 등록 기능 통합 테스트")
    void createReviewWithImages_Success() throws IOException {
        List<MultipartFile> images = Arrays.asList(
                new MockMultipartFile("images", "img1.jpg", "image/jpeg", "data1".getBytes()),
                new MockMultipartFile("images", "img2.jpg", "image/jpeg", "data2".getBytes()),
                new MockMultipartFile("images", "img3.jpg", "image/jpeg", "data3".getBytes()),
                new MockMultipartFile("images", "img4.jpg", "image/jpeg", "data4".getBytes()),
                new MockMultipartFile("images", "img5.jpg", "image/jpeg", "data5".getBytes())
        );

        ReviewRequest request = new ReviewRequest(
                testProperty.getId(),
                "실거주", 12, 5, 5, 5, 5, 5, 5,
                "매우 만족스러운 집입니다.", 1000, 50
        );

        long startTime = System.currentTimeMillis();
        ReviewResponse response = reviewService.createReview(testUser.getId(), request, images);
        long endTime = System.currentTimeMillis();

        assertThat(response.userId()).isEqualTo(testUser.getId());
        assertThat(response.imageUrls()).hasSize(5);
        
        System.out.println("\n========================================");
        System.out.println("테스트 결과 리포트");
        System.out.println("이미지 개수: " + images.size() + "장");
        System.out.println("총 소요 시간: " + (endTime - startTime) + "ms");
        System.out.println("========================================\n");
    }
}
