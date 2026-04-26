package com.comne.ejib.domain.property.repository;

import com.comne.ejib.domain.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
}
