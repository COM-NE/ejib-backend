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
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "C005", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "C006", "만료된 토큰입니다."),
    TOKEN_REUSE_DETECTED(HttpStatus.UNAUTHORIZED, "C007", "이미 사용된 리프레시 토큰입니다."),
    INVALID_LOGIN_TICKET(HttpStatus.UNAUTHORIZED, "C008", "유효하지 않은 로그인 티켓입니다."),

    // Auth (A)
    KAKAO_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A001", "카카오 로그인에 실패했습니다."),
    INVALID_KAKAO_OAUTH_STATE(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 카카오 OAuth state입니다."),
    INVALID_KAKAO_AUTHORIZATION_CODE(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 카카오 인가 코드입니다."),
    INVALID_KAKAO_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "유효하지 않은 카카오 토큰입니다."),
    KAKAO_API_ERROR(HttpStatus.BAD_GATEWAY, "A005", "카카오 API 호출 중 에러가 발생했습니다."),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "A006", "사용할 수 없는 닉네임입니다."),

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
    IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "I003", "파일 크기는 5MB를 초과할 수 없습니다."),

    // Property (P)
    PROPERTY_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "존재하지 않는 매물입니다."),

    // User (U)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 사용자입니다."),
    INVALID_ONBOARDING_PROFILE(HttpStatus.BAD_REQUEST, "U002", "profile은 blue, red, yellow 중 하나여야 합니다."),
    INVALID_ONBOARDING_STATUS(HttpStatus.BAD_REQUEST, "U003", "status는 student, job-seeker, worker, etc 중 하나여야 합니다."),
    INVALID_ONBOARDING_REQUIREMENT(HttpStatus.BAD_REQUEST, "U004", "requirement에 허용되지 않는 값이 포함되어 있습니다."),
    INVALID_ONBOARDING_REQUIREMENT_COUNT(HttpStatus.BAD_REQUEST, "U005", "requirement는 서로 다른 값 3개를 선택해야 합니다."),

    // Q&A (Q)
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Q001", "존재하지 않는 질문입니다."),
    NOT_VERIFIED_RESIDENT(HttpStatus.FORBIDDEN, "Q002", "실거주 인증된 사용자만 답변을 작성할 수 있습니다."),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "Q003", "존재하지 않는 답변입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
