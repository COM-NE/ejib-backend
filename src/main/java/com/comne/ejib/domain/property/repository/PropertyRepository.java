package com.comne.ejib.domain.property.repository;

import com.comne.ejib.domain.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    /**
     * 특정 지역의 매물과 해당 매물의 리뷰들을 한번에 조회합니다. (N+1 방지)
     *
     * @param region 조회할 지역 (예: "경기도 화성시")
     * @return 매물 리스트
     */
    @Query("SELECT DISTINCT p FROM Property p LEFT JOIN FETCH p.reviews WHERE p.address LIKE CONCAT(:region, '%') ESCAPE '\\'")
    List<Property> findAllWithReviewsByRegion(@Param("region") String region);
}
