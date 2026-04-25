package com.comne.ejib.domain.review.repository;

import com.comne.ejib.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPropertyId(Long propertyId);
    List<Review> findByUserId(Long userId);
    List<Review> findTop3ByOrderByCreatedAtDesc();
}
