package com.comne.ejib.domain.review.service;

import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;

/**
 * 실거주 인증을 위한 OCR 서비스
 * Google Vision API 연동 및 이미지 전처리를 통한 성능 최적화
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {
    private final ImageAnnotatorClient visionClient;

    // OCR 인식률과 용량 사이의 최적 지점 (A4 사이즈 계약서 기준 1500px 권장)
    private static final int TARGET_WIDTH = 1500;
    private static final float OUTPUT_QUALITY = 0.9f;

    // 파일 유효성 검증 상수
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final java.util.List<String> ALLOWED_TYPES =
            java.util.List.of("image/jpeg", "image/png", "image/webp");

    public void verifyContract(MultipartFile file, String userName, String address) {
        validateFile(file);

        try {
            // 1. 이미지 전처리: 리사이징 및 그레이스케일 변환
            byte[] processedImageBytes = preprocessImage(file);

            // 2. Google Vision API 호출
            String extractedText = requestOcrAnalysis(processedImageBytes);

            // 3. 유효성 검증
            validateOcrContent(extractedText, userName, address);

        } catch (IOException e) {
            log.error("파일 처리 중 IOException 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Thumbnailator를 이용한 이미지 최적화
     * 리사이징, 무채색화, JPEG 압축을 수행
     */
    private byte[] preprocessImage(MultipartFile file) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (InputStream inputStream = file.getInputStream()) {
            Thumbnails.of(inputStream)
                    .size(TARGET_WIDTH, TARGET_WIDTH) // 비율 유지하며 가로 최대 1500px로 조정
                    .imageType(BufferedImage.TYPE_BYTE_GRAY) // OCR 인식에 불필요한 색상 정보 제거
                    .outputFormat("jpg")
                    .outputQuality(OUTPUT_QUALITY) // 압축률 90% 유지
                    .toOutputStream(outputStream);
        }

        return outputStream.toByteArray();
    }

    /**
     * Vision API에 텍스트 추출 요청
     */
    private String requestOcrAnalysis(byte[] imageBytes) {
        ByteString imgBytes = ByteString.copyFrom(imageBytes);
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();

        BatchAnnotateImagesResponse response;
        try {
            response = visionClient.batchAnnotateImages(Collections.singletonList(request));
        } catch (RuntimeException e) {
            log.error("Vision API 호출 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OCR_VISION_API_ERROR);
        }

        // 응답 리스트가 비어있는지 확인
        if (response.getResponsesList().isEmpty()) {
            log.error("Vision API 응답 리스트가 비어있습니다.");
            throw new BusinessException(ErrorCode.OCR_VISION_API_ERROR);
        }

        AnnotateImageResponse res = response.getResponsesList().get(0);
        // 응답 내 에러 확인
        if (res.hasError()) {
            log.error("Vision API Runtime Error: {}", res.getError().getMessage());
            throw new BusinessException(ErrorCode.OCR_VISION_API_ERROR);
        }

        // FullTextAnnotation 및 Text null 체크
        return Optional.ofNullable(res.getFullTextAnnotation())
                .map(TextAnnotation::getText)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new BusinessException(ErrorCode.OCR_NO_TEXT_FOUND)); // 텍스트가 없으면 빈 문자열 반환 혹은 예외 처리
    }

    /**
     * 추출된 텍스트와 사용자 입력 정보 매칭 검증
     */
    private void validateOcrContent(String fullText, String userName, String address) {
        // 필수 입력값 검증
        if (!StringUtils.hasText(userName) || !StringUtils.hasText(address)) {
            log.warn("검증 실패: 사용자 입력 정보(이름/주소)가 누락되었습니다.");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (fullText == null || fullText.isBlank()) {
            throw new BusinessException(ErrorCode.OCR_NO_TEXT_FOUND);
        }

        // 공백 제거 후 비교 (전처리)
        String sanitizedText = fullText.replaceAll("\\s", "");
        String targetName = userName.replaceAll("\\s", "");
        String targetAddress = address.replaceAll("\\s", "");

        // 3. 포함 여부 검증
        boolean nameMatch = sanitizedText.contains(targetName);
        boolean addressMatch = sanitizedText.contains(targetAddress);
        if (!nameMatch || !addressMatch) {
            // 개인정보(PII)를 로그에 남기지 않도록 구체적인 데이터는 제외하고 상태만 기록
            log.warn("계약서 검증 불일치 - 이름 일치여부: {}, 주소 일치여부: {}", nameMatch, addressMatch);
            throw new BusinessException(ErrorCode.INVALID_CONTRACT_INFO);
        }

        log.info("OCR 인증 성공");
    }

    private void validateFile(MultipartFile file) {
        // 존재 여부 체크
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.OCR_FILE_EMPTY);
        }

        // 파일 크기 체크
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("파일 크기 초과: {} bytes", file.getSize());
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        // 파일 형식 체크
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            log.warn("지원하지 않는 파일 형식: {}", contentType);
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        try (InputStream is = file.getInputStream()) {
                        if (ImageIO.read(is) == null) {
                                log.warn("이미지 디코딩 실패: contentType={}", contentType);
                                throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
                            }
                    } catch (IOException e) {
                        log.warn("파일 시그니처 검증 실패", e);
                        throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
                    }
    }

}