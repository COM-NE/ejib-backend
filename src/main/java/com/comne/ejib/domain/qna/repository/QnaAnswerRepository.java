package com.comne.ejib.domain.qna.repository;

import com.comne.ejib.domain.qna.entity.QnaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Long> {
}
