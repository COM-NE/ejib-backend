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
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final Cloudinary cloudinary;
    private final TransactionTemplate transactionTemplate;

    /**
     * 새로운 리뷰를 등록합니다.
     * [개선사항]
     * 1. 외부 API(Cloudinary) 호출을 트랜잭션 외부로 분리하여 DB 커넥션 점유 시간 단축
     * 2. CompletableFuture를 활용하여 이미지를 병렬로 업로드 (N개 -> 1개 시간으로 단축)
     * 3. saveAll을 통한 Batch Insert 최적화
     */
    public ReviewResponse createReview(ReviewRequest request, List<MultipartFile> images) throws IOException {
        StopWatch stopWatch = new StopWatch("Review Creation Performance");

        // 0. 외부 업로드 이전에 참조 무결성 사전 검증 (이미지 누수 방지)
        if (!userRepository.existsById(request.userId())) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        if (!propertyRepository.existsById(request.propertyId())) {
            throw new IllegalArgumentException("존재하지 않는 매물입니다.");
        }

        // 1. 이미지 병렬 업로드 (DB 트랜잭션 외부에서 수행하여 커넥션 고갈 방지)
        stopWatch.start("Parallel Image Upload");
        List<String> imageUrls = uploadImagesParallel(images);
        stopWatch.stop();

        // 2. DB 저장 (필요한 구간만 트랜잭션 적용)
        stopWatch.start("DB Transactional Save");
        Review savedReview = transactionTemplate.execute(status -> {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
            Property property = propertyRepository.findById(request.propertyId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매물입니다."));

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

            Review result = reviewRepository.save(review);

            if (!imageUrls.isEmpty()) {
                List<ReviewImage> reviewImages = imageUrls.stream()
                        .map(url -> ReviewImage.builder()
                                .review(result)
                                .imageUrl(url)
                                .build())
                        .collect(Collectors.toList());
                reviewImageRepository.saveAll(reviewImages);
                // 응답 DTO 변환을 위해 영속성 컨텍스트 내 객체 상태 동기화
                reviewImages.forEach(img -> result.getImages().add(img));
            }
            return result;
        });
        stopWatch.stop();

        log.info("\n{}", stopWatch.prettyPrint());
        return ReviewResponse.from(savedReview);
    }

    /**
     * 이미지를 병렬로 Cloudinary에 업로드합니다.
     */
    private List<String> uploadImagesParallel(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }

        List<CompletableFuture<String>> futures = images.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    validateImage(file);
                    return uploadToCloudinary(file);
                }))
                .collect(Collectors.toList());

        try {
            return futures.stream()
                    .map(CompletableFuture::join) // 모든 업로드가 완료될 때까지 대기
                    .collect(Collectors.toList());
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if(cause instanceof BusinessException) throw (BusinessException) cause;
            if(cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw e;
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
