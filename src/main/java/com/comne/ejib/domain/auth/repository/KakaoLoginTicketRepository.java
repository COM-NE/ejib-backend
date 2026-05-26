package com.comne.ejib.domain.auth.repository;

import com.comne.ejib.domain.auth.entity.KakaoLoginTicket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface KakaoLoginTicketRepository extends JpaRepository<KakaoLoginTicket, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from KakaoLoginTicket t join fetch t.user where t.ticketHash = :ticketHash")
    Optional<KakaoLoginTicket> findByTicketHashForUpdate(@Param("ticketHash") String ticketHash);
}
