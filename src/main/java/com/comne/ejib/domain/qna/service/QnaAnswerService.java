package com.comne.ejib.domain.qna.service;

import com.comne.ejib.domain.qna.dto.QnaAnswerRequest;
import com.comne.ejib.domain.qna.dto.QnaAnswerResponse;
import com.comne.ejib.domain.qna.entity.QnaAnswer;
import com.comne.ejib.domain.qna.entity.QnaQuestion;
import com.comne.ejib.domain.qna.repository.QnaAnswerRepository;
import com.comne.ejib.domain.qna.repository.QnaQuestionRepository;
import com.comne.ejib.domain.review.repository.ReviewRepository;
import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.domain.user.repository.UserRepository;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QnaAnswerService {
    private final QnaAnswerRepository qnaAnswerRepository;
    private final QnaQuestionRepository qnaQuestionRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    /**
     * 질문에 대한 답변을 등록합니다. (실거주 인증 사용자 전용)
     */
    @Transactional
    public QnaAnswerResponse createAnswer(Long questionId, Long userId, QnaAnswerRequest request) {
        QnaQuestion question = qnaQuestionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 권한 검증: 해당 매물에 실거주 리뷰를 남긴 사용자인지 확인
        boolean isVerifiedResident = reviewRepository.existsByUserIdAndPropertyIdAndReviewType(
                user.getId(), question.getProperty().getId(), "실거주");

        if (!isVerifiedResident) {
            throw new BusinessException(ErrorCode.NOT_VERIFIED_RESIDENT);
        }

        QnaAnswer answer = QnaAnswer.builder()
                .question(question)
                .user(user)
                .content(request.getContent())
                .build();

        QnaAnswer savedAnswer = qnaAnswerRepository.save(answer);
        return QnaAnswerResponse.from(savedAnswer);
    }

    /**
     * 답변을 삭제합니다. 작성자만 삭제할 수 있습니다.
     */
    @Transactional
    public void deleteAnswer(Long answerId, Long userId) {
        QnaAnswer answer = qnaAnswerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANSWER_NOT_FOUND));

        if (!answer.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        qnaAnswerRepository.delete(answer);
    }
}
