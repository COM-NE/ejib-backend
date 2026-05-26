package com.comne.ejib.domain.qna.service;

import com.comne.ejib.domain.property.entity.Property;
import com.comne.ejib.domain.property.repository.PropertyRepository;
import com.comne.ejib.domain.qna.dto.QnaAnswerResponse;
import com.comne.ejib.domain.qna.dto.QnaQuestionRequest;
import com.comne.ejib.domain.qna.dto.QnaQuestionResponse;
import com.comne.ejib.domain.qna.entity.QnaAnswer;
import com.comne.ejib.domain.qna.entity.QnaQuestion;
import com.comne.ejib.domain.qna.repository.QnaAnswerRepository;
import com.comne.ejib.domain.qna.repository.QnaQuestionRepository;
import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.domain.user.repository.UserRepository;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QnaQuestionService {
    private final QnaQuestionRepository qnaQuestionRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    /**
     * 특정 매물에 대한 질문을 등록합니다.
     */
    @Transactional
    public QnaQuestionResponse createQuestion(Long propertyId, Long userId, QnaQuestionRequest request) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROPERTY_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        QnaQuestion question = QnaQuestion.builder()
                .property(property)
                .user(user)
                .content(request.getContent())
                .build();

        QnaQuestion savedQuestion = qnaQuestionRepository.save(question);
        return QnaQuestionResponse.from(savedQuestion, new ArrayList<>());
    }

    /**
     * 특정 매물의 모든 Q&A 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<QnaQuestionResponse> getQnaList(Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }

        List<QnaQuestion> questions = qnaQuestionRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId);
        
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = questions.stream().map(QnaQuestion::getId).toList();
        List<QnaAnswer> answers = qnaAnswerRepository.findAllByQuestionIdIn(questionIds);

        // 질문 ID별로 답변들을 매핑
        Map<Long, List<QnaAnswerResponse>> answersMap = answers.stream()
                .collect(Collectors.groupingBy(
                        answer -> answer.getQuestion().getId(),
                        Collectors.mapping(QnaAnswerResponse::from, Collectors.toList())
                ));

        return questions.stream()
                .map(q -> QnaQuestionResponse.from(q, answersMap.getOrDefault(q.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    /**
     * 질문을 삭제합니다. 작성자만 삭제할 수 있으며, 관련 답변도 모두 삭제됩니다.
     */
    @Transactional
    public void deleteQuestion(Long questionId, Long userId) {
        QnaQuestion question = qnaQuestionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        if (!question.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        qnaAnswerRepository.deleteByQuestionId(questionId);
        qnaQuestionRepository.delete(question);
    }
}
