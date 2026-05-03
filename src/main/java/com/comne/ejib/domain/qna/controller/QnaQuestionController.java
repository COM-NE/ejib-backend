package com.comne.ejib.domain.qna.controller;

import com.comne.ejib.domain.qna.dto.QnaQuestionRequest;
import com.comne.ejib.domain.qna.dto.QnaQuestionResponse;
import com.comne.ejib.domain.qna.service.QnaQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class QnaQuestionController {
    private final QnaQuestionService qnaQuestionService;

    /**
     * 특정 매물에 대한 질문을 등록합니다.
     */
    @PostMapping("/{propertyId}/questions")
    public ResponseEntity<QnaQuestionResponse> createQuestion(
            @PathVariable Long propertyId,
            @RequestBody @Valid QnaQuestionRequest request) {
        
        return ResponseEntity.ok(qnaQuestionService.createQuestion(propertyId, request));
    }

    /**
     * 특정 매물의 모든 Q&A 목록을 조회합니다.
     */
    @GetMapping("/{propertyId}/qna")
    public ResponseEntity<List<QnaQuestionResponse>> getQnaList(@PathVariable Long propertyId) {
        return ResponseEntity.ok(qnaQuestionService.getQnaList(propertyId));
    }
}
