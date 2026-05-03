package com.comne.ejib.domain.qna.dto;

import com.comne.ejib.domain.qna.entity.QnaQuestion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class QnaQuestionResponse {
    private Long id;
    private Long userId;
    private String userNickname;
    private String content;
    private List<QnaAnswerResponse> answers;
    private LocalDateTime createdAt;

    public static QnaQuestionResponse from(QnaQuestion question, List<QnaAnswerResponse> answers) {
        return QnaQuestionResponse.builder()
                .id(question.getId())
                .userId(question.getUser().getId())
                .userNickname(question.getUser().getNickname())
                .content(question.getContent())
                .answers(answers)
                .createdAt(question.getCreatedAt())
                .build();
    }
}
