package com.docuflow.api.service;

import com.docuflow.api.config.AppProperties;
import com.docuflow.api.entity.RefreshToken;
import com.docuflow.api.entity.User;
import com.docuflow.api.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional; // Don't forget this import
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties appProperties;

    /**
     * Finds a refresh token in the database by its string value.
     * Required by your Controller's functional stream.
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public String createRefreshToken(User user) {
        // Delete old tokens so you only ever have one active session per user device
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString()) // Random string, completely secure!
                .expiryDate(Instant.now().plusMillis(appProperties.getJwt().getRefreshTokenExpiryMs()))
                .build();

        return refreshTokenRepository.save(refreshToken).getToken();
    }
    
    /**
     * Checks if a token is expired. If it is, it purges it from the DB.
     * Must be @Transactional because it executes a delete operation if expired.
     */
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token was expired. Please sign in again.");
        }
        return token;
    }
}