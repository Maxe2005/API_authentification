package com.imt.API_authentification.service;

import com.imt.API_authentification.persistence.dao.RevokedTokenMongoDAO;
import com.imt.API_authentification.persistence.dto.RevokedTokenMongoDTO;
import com.imt.API_authentification.utils.AuthHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private final RevokedTokenMongoDAO revokedTokenMongoDAO;
    private final AuthHandler authHandler;

    public void revoke(String token) {
        Instant expiresAt = authHandler.getExpirationDate(token).atZone(ZoneId.systemDefault()).toInstant();
        revokedTokenMongoDAO.save(new RevokedTokenMongoDTO(hash(token), expiresAt));
    }

    public boolean isRevoked(String token) {
        return revokedTokenMongoDAO.existsById(hash(token));
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
