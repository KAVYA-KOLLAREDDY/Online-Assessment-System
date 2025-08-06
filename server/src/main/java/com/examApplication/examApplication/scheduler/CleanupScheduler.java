package com.examApplication.examApplication.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.examApplication.examApplication.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CleanupScheduler {
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void purgeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime staleThreshold = now.minusDays(30);
        refreshTokenRepository.deleteAllByExpiryDateBeforeOrRevokedIsTrueOrLastUsedAtBefore(LocalDateTime.now(),
                staleThreshold);
    }
}
