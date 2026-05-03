package com.comne.ejib.domain.qna.repository;

import com.comne.ejib.domain.qna.entity.QnaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Long> {
    /**
     * 질문 ID 목록에 해당하는 모든 답변을 사용자 정보와 함께 조회합니다.
     */
    @Query("SELECT a FROM QnaAnswer a JOIN FETCH a.user WHERE a.question.id IN :questionIds")
    List<QnaAnswer> findAllByQuestionIdIn(@Param("questionIds") List<Long> questionIds);
}
