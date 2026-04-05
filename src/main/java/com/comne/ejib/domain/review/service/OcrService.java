package com.comne.ejib.domain.review.service;

import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

        Thumbnails.of(file.getInputStream())
                .size(TARGET_WIDTH, TARGET_WIDTH) // 비율 유지하며 가로 최대 1500px로 조정
                .imageType(BufferedImage.TYPE_BYTE_GRAY) // OCR 인식에 불필요한 색상 정보 제거
                .outputFormat("jpg")
                .outputQuality(OUTPUT_QUALITY) // 압축률 90% 유지
                .toOutputStream(outputStream);

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

        BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(Collections.singletonList(request));
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
                .orElse(""); // 텍스트가 없으면 빈 문자열 반환 혹은 예외 처리
    }

    /**
     * 추출된 텍스트와 사용자 입력 정보 매칭 검증
     */
    private void validateOcrContent(String fullText, String userName, String address) {
        if (fullText == null || fullText.isBlank()) {
            throw new BusinessException(ErrorCode.OCR_NO_TEXT_FOUND);
        }

        // 공백 제거 후 비교
        String sanitizedText = fullText.replaceAll("\\s", "");
        String targetName = userName.replaceAll("\\s", "");
        String targetAddress = address.replaceAll("\\s", "");

        if (!sanitizedText.contains(targetName) || !sanitizedText.contains(targetAddress)) {
            log.warn("텍스트 검증 불일치");
            throw new BusinessException(ErrorCode.INVALID_CONTRACT_INFO);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.OCR_FILE_EMPTY);
        }
    }

}