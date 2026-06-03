package com.comne.ejib.domain.review.repository;

import com.comne.ejib.domain.property.dto.PropertyImageProjection;
import com.comne.ejib.domain.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    @Query("""
            SELECT ri.id AS id, ri.imageUrl AS imageUrl FROM ReviewImage ri
            WHERE ri.review.property.id = :propertyId
            """)
    List<PropertyImageProjection> findAllByPropertyId(@Param("propertyId") Long propertyId);
}
