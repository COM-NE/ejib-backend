package com.comne.ejib.domain.property.repository;

import com.comne.ejib.domain.property.dto.PropertyDetailProjection;
import com.comne.ejib.domain.property.entity.Property;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    @Query("""
            SELECT
                p.propertyName AS propertyName,
                p.address AS propertyAddress,
                COALESCE((
                    SELECT AVG(r.totalScore) FROM Review r WHERE r.property = p
                ), 0) AS averageTotalScore,
                (
                    SELECT COUNT(r) FROM Review r WHERE r.property = p
                ) AS reviewCount,
                p.propertyType AS propertyType,
                p.transactionType AS transactionType,
                p.monthlyRent AS monthlyRent,
                p.deposit AS deposit,
                p.floor AS floor,
                p.area AS area,
                p.agency AS agency,
                p.distanceToSchool AS distanceToSchool,
                p.description AS description
            FROM Property p WHERE p.id = :propertyId
            """)
    Optional<PropertyDetailProjection> findDetailById(@Param("propertyId") Long propertyId);

    /**
     * 특정 지역의 매물 ID들을 페이징하여 조회합니다.
     */
    @Query("SELECT p.id FROM Property p WHERE p.address LIKE CONCAT(:region, '%') ESCAPE '\\'")
    List<Long> findIdsByRegion(@Param("region") String region, Pageable pageable);

    /**
     * ID 목록에 해당하는 매물과 리뷰들을 한번에 조회합니다. (N+1 방지)
     */
    @Query("SELECT DISTINCT p FROM Property p LEFT JOIN FETCH p.reviews WHERE p.id IN :ids")
    List<Property> findAllWithReviewsByIdIn(@Param("ids") List<Long> ids);

    /**
     * (기존 메서드 - 유지하되 내부적으로는 위 2단계 방식을 권장)
     */
    @Query("SELECT DISTINCT p FROM Property p LEFT JOIN FETCH p.reviews WHERE p.address LIKE CONCAT(:region, '%') ESCAPE '\\'")
    List<Property> findAllWithReviewsByRegion(@Param("region") String region);
}
