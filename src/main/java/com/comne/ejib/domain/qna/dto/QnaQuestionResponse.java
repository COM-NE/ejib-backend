package com.comne.ejib.domain.qna.dto;

import com.comne.ejib.domain.qna.entity.QnaQuestion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QnaQuestionResponse {
    private Long id;
    private Long userId;
    private String userNickname;
    private String content;
    private LocalDateTime createdAt;

    public static QnaQuestionResponse from(QnaQuestion question) {
        return QnaQuestionResponse.builder()
                .id(question.getId())
                .userId(question.getUser().getId())
                .userNickname(question.getUser().getNickname())
                .content(question.getContent())
                .createdAt(question.getCreatedAt())
                .build();
    }
}
