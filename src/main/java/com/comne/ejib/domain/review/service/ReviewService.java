package com.comne.ejib.domain.review.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.comne.ejib.domain.property.entity.Property;
import com.comne.ejib.domain.property.repository.PropertyRepository;
import com.comne.ejib.domain.review.dto.ReviewRequest;
import com.comne.ejib.domain.review.entity.Review;
import com.comne.ejib.domain.review.entity.ReviewImage;
import com.comne.ejib.domain.review.repository.ReviewImageRepository;
import com.comne.ejib.domain.review.repository.ReviewRepository;
import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.domain.user.repository.UserRepository;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final Cloudinary cloudinary;

    /**
     * 새로운 리뷰를 등록하고 관련 이미지를 서버에 업로드합니다.
     *
     * @param request 리뷰 정보
     * @param images  리뷰와 함께 업로드할 이미지 파일 리스트
     * @return 생성된 리뷰의 식별자(ID)
     * @throws IOException 이미지 파일 읽기 실패 시 발생
     */
    @Transactional
    public Long createReview(ReviewRequest request, List<MultipartFile> images) throws IOException {
        // 1. 데이터 정합성 확인: 존재하지 않는 유저나 매물일 경우 예외 발생
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매물입니다."));

        // 2. Review 엔티티 생성 및 저장
        Review review = Review.builder()
                .user(user)
                .property(property)
                .reviewType(request.reviewType())
                .residenceDuration(request.residenceDuration())
                .totalScore(request.totalScore())
                .houseScore(request.houseScore())
                .facilityScore(request.facilityScore())
                .infraScore(request.infraScore())
                .safetyScore(request.safetyScore())
                .envScore(request.envScore())
                .content(request.content())
                .deposit(request.deposit())
                .monthlyRent(request.monthlyRent())
                .build();
        reviewRepository.save(review);

        // 3. 첨부된 이미지가 있을 경우 업로드 및 DB 기록 프로세스 진행
        if (images != null && !images.isEmpty()) {
            processImages(images, review);
        }

        return review.getId();
    }

    /**
     * 이미지 리스트를 순회하며 검증, 업로드 및 엔티티 저장을 수행합니다.
     */
    private void processImages(List<MultipartFile> images, Review review) {
        for (MultipartFile file : images) {
            // 파일 유효성 검사 (파일 미첨부, 용량 초과 등)
            validateImage(file);

            // Cloudinary에 이미지 업로드
            String imageUrl = uploadToCloudinary(file);

            // 업로드된 URL을 바탕으로 ReviewImage 엔티티 생성 및 저장
            ReviewImage reviewImage = ReviewImage.builder()
                    .review(review)
                    .imageUrl(imageUrl)
                    .build();
            reviewImageRepository.save(reviewImage);
        }
    }

    /**
     * Cloudinary 서버로 이미지를 전송하고 업로드된 URL을 반환받습니다.
     *
     * @param file 업로드할 멀티파트 파일
     * @return 업로드된 이미지의 공개 URL
     */
    private String uploadToCloudinary(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return (String) uploadResult.get("url");
        } catch (Exception e) {
            log.error("Cloudinary upload error for file: {}", file.getOriginalFilename(), e);
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_ERROR);
        }
    }

    /**
     * 이미지 파일의 유효성을 검사합니다.
     * 현재는 파일 비어있음 확인 및 5MB 용량 제한을 수행합니다.
     */
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_FILE_EMPTY);
        }

        // 5MB 용량 제한 (1024 * 1024 * 5)
        if (file.getSize() > 5 * 1024 * 1024) {
            log.warn("파일 용량 초과: {} (Size: {} bytes)", file.getOriginalFilename(), file.getSize());
            throw new BusinessException(ErrorCode.IMAGE_SIZE_EXCEEDED);
        }
    }
}
