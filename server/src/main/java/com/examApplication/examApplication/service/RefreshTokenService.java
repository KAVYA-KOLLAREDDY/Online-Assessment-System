package com.examApplication.examApplication.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.examApplication.examApplication.entity.RefreshToken;
import com.examApplication.examApplication.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository tokenRepository;

    public RefreshToken getToken(String token) {
        RefreshToken refreshToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Refresh Token!"));
        return refreshToken;

    }

    public void revokeToken(String refreshToken) {
        RefreshToken token = this.getToken(refreshToken);
        token.setRevoked(true);
        token.setRevokedAt(LocalDateTime.now());
        tokenRepository.save(token);
    }
}
