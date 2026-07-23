package com.imt.API_authentification.service;

import com.imt.API_authentification.persistence.dao.RevokedTokenMongoDAO;
import com.imt.API_authentification.persistence.dto.RevokedTokenMongoDTO;
import com.imt.API_authentification.utils.AuthHandler;
import com.imt.API_authentification.utils.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private final RevokedTokenMongoDAO revokedTokenMongoDAO;
    private final AuthHandler authHandler;

    public void revoke(String token) {
        Instant expiresAt = authHandler.getExpirationDate(token).atZone(ZoneId.systemDefault()).toInstant();
        revokedTokenMongoDAO.save(new RevokedTokenMongoDTO(TokenHasher.sha256Hex(token), expiresAt));
    }

    public boolean isRevoked(String token) {
        return revokedTokenMongoDAO.existsById(TokenHasher.sha256Hex(token));
    }
}
