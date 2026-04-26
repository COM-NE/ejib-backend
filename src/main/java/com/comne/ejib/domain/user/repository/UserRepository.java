package com.comne.ejib.domain.user.repository;

import com.comne.ejib.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
