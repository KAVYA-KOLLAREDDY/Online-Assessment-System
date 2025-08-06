package com.examApplication.examApplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.examApplication.examApplication.entity.RefreshToken;

import jakarta.transaction.Transactional;

import java.util.Optional;
import java.time.LocalDateTime;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken t WHERE t.expiryDate < :now OR t.revoked = true OR (t.lastUsedAt IS NULL OR t.lastUsedAt < :staleThreshold)")
    void deleteAllByExpiryDateBeforeOrRevokedIsTrueOrLastUsedAtBefore(@Param("now") LocalDateTime now,
            @Param("staleThreshold") LocalDateTime staleThreshold);

    void deleteByExpiryDateBefore(LocalDateTime date);
}
