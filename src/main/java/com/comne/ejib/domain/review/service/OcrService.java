package com.comne.ejib.domain.review.service;

import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {
    private final ImageAnnotatorClient visionClient;

    public void verifyContract(MultipartFile file, String userName, String address) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.OCR_FILE_EMPTY);
        }

        try {
            ByteString imgBytes = ByteString.copyFrom(file.getBytes());
            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();

            BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(Collections.singletonList(request));
            AnnotateImageResponse res = response.getResponsesList().get(0);

            if (res.hasError()) {
                log.error("Vision API 호출 에러: {}", res.getError().getMessage());
                throw new BusinessException(ErrorCode.OCR_VISION_API_ERROR);
            }

            String fullText = res.getFullTextAnnotation().getText();
            if (fullText == null || fullText.isBlank()) {
                throw new BusinessException(ErrorCode.OCR_NO_TEXT_FOUND);
            }

            String sanitizedText = fullText.replaceAll("\\s", "");
            String targetName = userName.replaceAll("\\s", "");
            String targetAddress = address.replaceAll("\\s", "");

            if (!sanitizedText.contains(targetName) || !sanitizedText.contains(targetAddress)) {
                log.warn("OCR 검증 불일치 - 입력된 이름: {}, 주소: {}", targetName, targetAddress);
                throw new BusinessException(ErrorCode.INVALID_CONTRACT_INFO);
            }

            log.info("OCR 검증 성공 - 사용자: {}", userName);

        } catch (IOException e) {
            log.error("파일 처리 중 IOException 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}