package com.comne.ejib.domain.qna.controller;

import com.comne.ejib.domain.qna.dto.QnaAnswerRequest;
import com.comne.ejib.domain.qna.dto.QnaAnswerResponse;
import com.comne.ejib.domain.qna.service.QnaAnswerService;
import com.comne.ejib.global.security.support.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class QnaAnswerController {
    private final QnaAnswerService qnaAnswerService;

    /**
     * 질문에 대한 답변을 등록합니다.
     */
    @PostMapping("/questions/{questionId}/answers")
    public ResponseEntity<QnaAnswerResponse> createAnswer(
            @PathVariable Long questionId,
            @RequestBody @Valid QnaAnswerRequest request) {
        
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(qnaAnswerService.createAnswer(questionId, userId, request));
    }

    /**
     * 특정 답변을 삭제합니다.
     */
    @DeleteMapping("/answers/{answerId}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable Long answerId) {
        
        Long userId = SecurityUtil.getCurrentUserId();
        qnaAnswerService.deleteAnswer(answerId, userId);
        return ResponseEntity.noContent().build();
    }
}
