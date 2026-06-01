package com.comne.ejib.domain.review.repository;

import com.comne.ejib.domain.review.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // ID만 조회하는 쿼리들 (이미지 조인 X -> 페이징/중복 문제 없음)
    @Query("select r.id from Review r where r.property.id = :propertyId")
    List<Long> findIdsByPropertyId(@Param("propertyId") Long propertyId);

    @Query("select r.id from Review r where r.user.id = :userId")
    List<Long> findIdsByUserId(@Param("userId") Long userId);

    @Query("select r.id from Review r order by r.createdAt desc")
    List<Long> findLatestIds(Pageable pageable);

    // ID 묶음으로 실제 데이터를 가져오는 쿼리 (이미지, 유저 조인 O)
    @EntityGraph(attributePaths = {"images", "user"})
    List<Review> findByIdIn(Collection<Long> ids, Sort sort);

    /**
     * 특정 사용자가 특정 매물에 대해 특정 타입의 리뷰를 작성했는지 확인합니다.
     */
    boolean existsByUserIdAndPropertyIdAndReviewType(Long userId, Long propertyId, String reviewType);
}
