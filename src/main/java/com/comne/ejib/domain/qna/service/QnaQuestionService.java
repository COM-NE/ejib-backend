package com.comne.ejib.domain.qna.service;

import com.comne.ejib.domain.property.entity.Property;
import com.comne.ejib.domain.property.repository.PropertyRepository;
import com.comne.ejib.domain.qna.dto.QnaQuestionRequest;
import com.comne.ejib.domain.qna.dto.QnaQuestionResponse;
import com.comne.ejib.domain.qna.entity.QnaQuestion;
import com.comne.ejib.domain.qna.repository.QnaQuestionRepository;
import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.domain.user.repository.UserRepository;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QnaQuestionService {
    private final QnaQuestionRepository qnaQuestionRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    /**
     * 특정 매물에 대한 질문을 등록합니다.
     */
    @Transactional
    public QnaQuestionResponse createQuestion(Long propertyId, QnaQuestionRequest request) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROPERTY_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        QnaQuestion question = QnaQuestion.builder()
                .property(property)
                .user(user)
                .content(request.getContent())
                .build();

        QnaQuestion savedQuestion = qnaQuestionRepository.save(question);
        return QnaQuestionResponse.from(savedQuestion);
    }
}
