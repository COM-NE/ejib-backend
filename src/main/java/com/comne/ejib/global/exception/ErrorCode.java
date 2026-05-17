package com.comne.ejib.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 공통 (C)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "적절하지 않은 입력값입니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C002", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 에러입니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "C004", "인증이 필요합니다."),

    // OCR (O)
    OCR_FILE_EMPTY(HttpStatus.BAD_REQUEST, "O001", "업로드된 파일이 비어있습니다."),
    OCR_VISION_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "O002", "Google Vision API 호출 중 에러가 발생했습니다."),
    OCR_NO_TEXT_FOUND(HttpStatus.BAD_REQUEST, "O003", "이미지에서 텍스트를 추출할 수 없습니다."),
    INVALID_CONTRACT_INFO(HttpStatus.BAD_REQUEST, "O004", "계약서 정보가 사용자 정보와 일치하지 않습니다."),
    OCR_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "O005", "업로드 가능한 파일 크기를 초과했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "O006", "지원하지 않는 파일 형식입니다."),

    // Image (I)
    IMAGE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "I001", "이미지 업로드 중 에러가 발생했습니다."),
    IMAGE_FILE_EMPTY(HttpStatus.BAD_REQUEST, "I002", "업로드된 파일이 비어있습니다."),
    IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "I003", "파일 크기는 5MB를 초과할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
