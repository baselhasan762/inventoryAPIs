package com.inventoryAPIs.demo.service;

import com.inventoryAPIs.demo.entity.RefreshToken;
import com.inventoryAPIs.demo.entity.User;
import com.inventoryAPIs.demo.exception.InvalidRefreshTokenException;
import com.inventoryAPIs.demo.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Refresh tokens are random strings handed to the client once; only their SHA-256 hash is stored,
 * so a database leak gives no usable tokens.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    /** Returns the raw token for the client; only its hash is saved. */
    public String create(User user) {
        String token = generateToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(hash(token))
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build();
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    /**
     * Validates a refresh token and uses it up (rotation). Presenting an already revoked token means it was
     * stolen or replayed, so every token of that user is revoked. That revoke must survive the exception,
     * hence {@code noRollbackFor}.
     */
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public User consume(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            refreshTokenRepository.revokeAllByUser(refreshToken.getUser());
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }
        refreshToken.setRevoked(true);
        return refreshToken.getUser();
    }

    /** Logout: revokes the token if it exists; unknown or already revoked tokens are ignored, so it never fails. */
    public void revoke(String token) {
        refreshTokenRepository.findByTokenHash(hash(token))
                .ifPresent(refreshToken -> refreshToken.setRevoked(true));
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
