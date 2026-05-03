package com.comne.ejib.domain.qna.dto;

import com.comne.ejib.domain.qna.entity.QnaAnswer;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QnaAnswerResponse {
    private Long id;
    private Long userId;
    private String userNickname;
    private String content;
    private LocalDateTime createdAt;

    public static QnaAnswerResponse from(QnaAnswer answer) {
        return QnaAnswerResponse.builder()
                .id(answer.getId())
                .userId(answer.getUser().getId())
                .userNickname(answer.getUser().getNickname())
                .content(answer.getContent())
                .createdAt(answer.getCreatedAt())
                .build();
    }
}
