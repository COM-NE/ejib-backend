package com.comne.ejib.domain.qna.repository;

import com.comne.ejib.domain.qna.entity.QnaQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QnaQuestionRepository extends JpaRepository<QnaQuestion, Long> {
    List<QnaQuestion> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);
}
