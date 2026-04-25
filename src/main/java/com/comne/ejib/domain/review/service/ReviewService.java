package com.comne.ejib.domain.review.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.comne.ejib.domain.property.entity.Property;
import com.comne.ejib.domain.property.repository.PropertyRepository;
import com.comne.ejib.domain.review.dto.ReviewRequest;
import com.comne.ejib.domain.review.dto.ReviewResponse;
import com.comne.ejib.domain.review.entity.Review;
import com.comne.ejib.domain.review.entity.ReviewImage;
import com.comne.ejib.domain.review.repository.ReviewImageRepository;
import com.comne.ejib.domain.review.repository.ReviewRepository;
import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.domain.user.repository.UserRepository;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * @return 생성된 리뷰 정보 (DTO)
     * @throws IOException 이미지 파일 읽기 실패 시 발생
     */
    @Transactional
    public ReviewResponse createReview(ReviewRequest request, List<MultipartFile> images) throws IOException {
        // 1. 데이터 정합성 확인
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
        
        // save() 후 반환된 엔티티를 사용 (ID가 생성됨)
        Review savedReview = reviewRepository.save(review);

        // 3. 첨부된 이미지가 있을 경우 업로드 및 DB 기록
        if (images != null && !images.isEmpty()) {
            processImages(images, savedReview);
        }

        // 4. DTO로 변환하여 반환
        return ReviewResponse.from(savedReview);
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

    /**
     * 특정 매물(Property)에 등록된 모든 리뷰를 조회합니다.
     *
     * @param propertyId 조회할 매물의 식별자(ID)
     * @return 해당 매물에 등록된 리뷰 정보 리스트 (DTO)
     * @throws IllegalArgumentException 존재하지 않는 매물 ID일 경우 발생
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByPropertyId(Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new IllegalArgumentException("존재하지 않는 매물입니다.");
        }
        return reviewRepository.findByPropertyId(propertyId).stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 유저(User)가 작성한 모든 리뷰를 조회합니다.
     *
     * @param userId 조회할 유저의 식별자(ID)
     * @return 해당 유저가 작성한 리뷰 정보 리스트 (DTO)
     * @throws IllegalArgumentException 존재하지 않는 유저 ID일 경우 발생
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        return reviewRepository.findByUserId(userId).stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 가장 최근에 등록된 리뷰 3개를 조회합니다.
     *
     * @return 최신 리뷰 3개 정보 리스트 (DTO)
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getLatestReviews() {
        return reviewRepository.findTop3ByOrderByCreatedAtDesc().stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }
}
