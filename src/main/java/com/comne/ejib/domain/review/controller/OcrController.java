package com.comne.ejib.domain.review.controller;


import com.comne.ejib.domain.review.dto.OcrVerifyResponse;
import com.comne.ejib.domain.review.service.OcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class OcrController {

    private final OcrService ocrService;

    /**
     * 계약서 OCR 검증 API
     * @param file 사용자가 업로드한 이미지 파일
     * @param userName 사용자 이름 (추후 변경)
     * @param address 검증할 주소 (추후 변경)
     */
    @PostMapping("/verify-contract")
    public ResponseEntity<OcrVerifyResponse> verifyContract(
            @RequestPart("file") MultipartFile file,
            @RequestParam("userName") String userName,
            @RequestParam("address") String address) {

        ocrService.verifyContract(file, userName, address);

        return ResponseEntity.ok(new OcrVerifyResponse(true, "계약서 인증에 성공했습니다."));
    }
}
