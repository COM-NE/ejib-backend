package com.comne.ejib.domain.user.repository;

import com.comne.ejib.domain.user.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    void deleteByUserId(Long userId);

    List<UserPreference> findAllByUserId(Long userId);
}
