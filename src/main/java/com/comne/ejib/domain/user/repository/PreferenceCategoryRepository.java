package com.comne.ejib.domain.user.repository;

import com.comne.ejib.domain.user.entity.PreferenceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PreferenceCategoryRepository extends JpaRepository<PreferenceCategory, Long> {
    List<PreferenceCategory> findAllByNameIn(Collection<String> names);
}
