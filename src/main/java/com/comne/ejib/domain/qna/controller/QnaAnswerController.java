package com.comne.ejib.domain.qna.controller;

import com.comne.ejib.domain.qna.dto.QnaAnswerRequest;
import com.comne.ejib.domain.qna.dto.QnaAnswerResponse;
import com.comne.ejib.domain.qna.service.QnaAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QnaAnswerController {
    private final QnaAnswerService qnaAnswerService;

    /**
     * 질문에 대한 답변을 등록합니다.
     */
    @PostMapping("/{questionId}/answers")
    public ResponseEntity<QnaAnswerResponse> createAnswer(
            @PathVariable Long questionId,
            @RequestBody @Valid QnaAnswerRequest request) {
        
        return ResponseEntity.ok(qnaAnswerService.createAnswer(questionId, request));
    }
}
