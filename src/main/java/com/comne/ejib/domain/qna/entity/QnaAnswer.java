package com.comne.ejib.domain.qna.entity;

import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "qna_answers")
public class QnaAnswer extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private QnaQuestion question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
