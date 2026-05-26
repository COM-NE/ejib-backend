package com.comne.ejib.domain.qna.controller;

import com.comne.ejib.domain.qna.dto.QnaQuestionRequest;
import com.comne.ejib.domain.qna.dto.QnaQuestionResponse;
import com.comne.ejib.domain.qna.service.QnaQuestionService;
import com.comne.ejib.global.security.support.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class QnaQuestionController {
    private final QnaQuestionService qnaQuestionService;

    /**
     * 특정 매물에 대한 질문을 등록합니다.
     */
    @PostMapping("/properties/{propertyId}/questions")
    public ResponseEntity<QnaQuestionResponse> createQuestion(
            @PathVariable Long propertyId,
            @RequestBody @Valid QnaQuestionRequest request) {
        
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(qnaQuestionService.createQuestion(propertyId, userId, request));
    }

    /**
     * 특정 매물의 모든 Q&A 목록을 조회합니다.
     */
    @GetMapping("/properties/{propertyId}/qna")
    public ResponseEntity<List<QnaQuestionResponse>> getQnaList(@PathVariable Long propertyId) {
        return ResponseEntity.ok(qnaQuestionService.getQnaList(propertyId));
    }

    /**
     * 특정 질문을 삭제합니다.
     */
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long questionId) {
        
        Long userId = SecurityUtil.getCurrentUserId();
        qnaQuestionService.deleteQuestion(questionId, userId);
        return ResponseEntity.noContent().build();
    }
}
