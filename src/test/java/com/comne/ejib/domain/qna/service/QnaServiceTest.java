package com.comne.ejib.domain.qna.service;

import com.comne.ejib.domain.property.entity.Property;
import com.comne.ejib.domain.property.repository.PropertyRepository;
import com.comne.ejib.domain.qna.dto.QnaQuestionRequest;
import com.comne.ejib.domain.qna.dto.QnaQuestionResponse;
import com.comne.ejib.domain.qna.entity.QnaQuestion;
import com.comne.ejib.domain.qna.repository.QnaAnswerRepository;
import com.comne.ejib.domain.qna.repository.QnaQuestionRepository;
import com.comne.ejib.domain.user.entity.User;
import com.comne.ejib.domain.user.repository.UserRepository;
import com.comne.ejib.global.exception.BusinessException;
import com.comne.ejib.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class QnaServiceTest {

    @Autowired
    private QnaQuestionService qnaQuestionService;

    @Autowired
    private QnaAnswerService qnaAnswerService;

    @Autowired
    private QnaQuestionRepository qnaQuestionRepository;

    @Autowired
    private QnaAnswerRepository qnaAnswerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private com.comne.ejib.domain.review.repository.ReviewRepository reviewRepository;

    private User questionAuthor;
    private User answerAuthor;
    private User otherUser;
    private Property testProperty;

    @BeforeEach
    void setUp() {
        questionAuthor = userRepository.save(User.builder()
                .nickname("질문자")
                .profileImage(1)
                .jobType("학생")
                .point(0)
                .kakaoId("kakao_1")
                .build());

        answerAuthor = userRepository.save(User.builder()
                .nickname("답변자")
                .profileImage(1)
                .jobType("학생")
                .point(0)
                .kakaoId("kakao_answer")
                .build());

        otherUser = userRepository.save(User.builder()
                .nickname("다른사람")
                .profileImage(1)
                .jobType("학생")
                .point(0)
                .kakaoId("kakao_2")
                .build());

        testProperty = propertyRepository.save(Property.builder()
                .address("서울시")
                .build());
        
        // 답변자는 실거주 인증된 사용자여야 함
        reviewRepository.save(com.comne.ejib.domain.review.entity.Review.builder()
                .user(answerAuthor)
                .property(testProperty)
                .reviewType("실거주")
                .content("인증용 리뷰")
                .build());
    }

    @Test
    @DisplayName("질문 삭제 성공")
    void deleteQuestion_Success() {
        // given
        QnaQuestionRequest request = new QnaQuestionRequest(questionAuthor.getId(), "질문 내용");
        QnaQuestionResponse response = qnaQuestionService.createQuestion(testProperty.getId(), request);
        Long questionId = response.getId();

        // when
        qnaQuestionService.deleteQuestion(questionId, questionAuthor.getId());

        // then
        assertThat(qnaQuestionRepository.findById(questionId)).isEmpty();
    }

    @Test
    @DisplayName("작성자가 아닌 경우 질문 삭제 실패")
    void deleteQuestion_Fail_NotAuthor() {
        // given
        QnaQuestionRequest request = new QnaQuestionRequest(questionAuthor.getId(), "질문 내용");
        QnaQuestionResponse response = qnaQuestionService.createQuestion(testProperty.getId(), request);
        Long questionId = response.getId();

        // when & then
        assertThatThrownBy(() -> qnaQuestionService.deleteQuestion(questionId, otherUser.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HANDLE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("존재하지 않는 매물 ID로 Q&A 목록 조회 시 실패")
    void getQnaList_Fail_PropertyNotFound() {
        // given
        Long invalidPropertyId = 9999L;

        // when & then
        assertThatThrownBy(() -> qnaQuestionService.getQnaList(invalidPropertyId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROPERTY_NOT_FOUND);
    }

    @Test
    @DisplayName("답변 삭제 성공")
    void deleteAnswer_Success() {
        // given
        QnaQuestionRequest qRequest = new QnaQuestionRequest(questionAuthor.getId(), "질문");
        QnaQuestionResponse qResponse = qnaQuestionService.createQuestion(testProperty.getId(), qRequest);
        
        com.comne.ejib.domain.qna.dto.QnaAnswerRequest aRequest = new com.comne.ejib.domain.qna.dto.QnaAnswerRequest(answerAuthor.getId(), "답변");
        com.comne.ejib.domain.qna.dto.QnaAnswerResponse aResponse = qnaAnswerService.createAnswer(qResponse.getId(), aRequest);
        Long answerId = aResponse.getId();

        // when
        qnaAnswerService.deleteAnswer(answerId, answerAuthor.getId());

        // then
        assertThat(qnaAnswerRepository.findById(answerId)).isEmpty();
    }
}
