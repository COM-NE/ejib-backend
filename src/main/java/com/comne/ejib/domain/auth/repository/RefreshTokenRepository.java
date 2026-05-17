package com.comne.ejib.domain.auth.repository;

import com.comne.ejib.domain.auth.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rt from RefreshToken rt join fetch rt.user where rt.tokenId = :tokenId")
    Optional<RefreshToken> findByTokenIdForUpdate(@Param("tokenId") String tokenId);

    List<RefreshToken> findAllByUserIdAndRevokedAtIsNull(Long userId);
}
