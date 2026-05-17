package com.comne.ejib.domain.auth.repository;

import com.comne.ejib.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenId(String tokenId);

    List<RefreshToken> findAllByUserIdAndRevokedAtIsNull(Long userId);
}
