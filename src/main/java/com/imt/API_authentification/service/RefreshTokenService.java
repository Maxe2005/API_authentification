package com.imt.API_authentification.service;

import com.imt.API_authentification.exception.TokenInvalidException;
import com.imt.API_authentification.persistence.dao.RefreshTokenMongoDAO;
import com.imt.API_authentification.persistence.dto.RefreshTokenMongoDTO;
import com.imt.API_authentification.utils.SecurityProperties;
import com.imt.API_authentification.utils.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 64;

    private final RefreshTokenMongoDAO refreshTokenMongoDAO;
    private final SecurityProperties securityProperties;

    public record RefreshResult(String username, String refreshToken) {
    }

    public String issue(String username) {
        String rawToken = generateRawToken();
        Instant expiresAt = Instant.now().plus(securityProperties.getRefreshTokenTtlDays(), ChronoUnit.DAYS);
        refreshTokenMongoDAO.save(new RefreshTokenMongoDTO(TokenHasher.sha256Hex(rawToken), username, expiresAt));
        return rawToken;
    }

    public RefreshResult rotate(String rawToken) throws TokenInvalidException {
        RefreshTokenMongoDTO existing = refreshTokenMongoDAO.findById(TokenHasher.sha256Hex(rawToken)).orElse(null);
        if (existing == null) throw new TokenInvalidException("Invalid refresh token");

        if (existing.isRevoked()) {
            // A previously-rotated (or logged-out) refresh token being reused is a strong signal
            // of token theft: kill every outstanding refresh token for that user, not just this one.
            revokeAllForUser(existing.getUsername());
            throw new TokenInvalidException("Refresh token has already been used");
        }
        if (existing.getExpiresAt().isBefore(Instant.now())) throw new TokenInvalidException("Refresh token expired");

        existing.setRevoked(true);
        refreshTokenMongoDAO.save(existing);

        return new RefreshResult(existing.getUsername(), issue(existing.getUsername()));
    }

    public void revoke(String rawToken) {
        refreshTokenMongoDAO.deleteById(TokenHasher.sha256Hex(rawToken));
    }

    public void revokeAllForUser(String username) {
        refreshTokenMongoDAO.deleteByUsername(username);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
