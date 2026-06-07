package com.comne.ejib.domain.property.repository;

import com.comne.ejib.domain.property.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    Optional<Scrap> findByUserIdAndPropertyId(Long userId, Long propertyId);
}
