package com.imt.API_authentification.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.imt.API_authentification.persistence.dto.Role;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;

@Component
public class AuthHandler {

    // Dedicated to the internal token format only — intentionally decoupled from any
    // HTTP-layer/Spring-managed ObjectMapper so a future web Jackson config change can
    // never silently break token (de)serialization.
    private static final ObjectMapper TOKEN_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final SecretKey key;

    public AuthHandler(SecurityProperties secure) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (secure.getSecret() == null || secure.getSecret().isBlank()) {
            throw new IllegalStateException("Missing security secret. Configure it in application.yml / env.");
        }
        if (secure.getSalt() == null || secure.getSalt().isBlank()) {
            throw new IllegalStateException("Missing security salt. Configure it in application.yml / env.");
        }
        this.key = AESUtil.getKeyFromPassword(secure.getSecret(), secure.getSalt());
    }

    public String generateToken(String username, Role role) {
        try {
            String token = TOKEN_MAPPER.writeValueAsString(new Token(username, role));
            return AESUtil.encryptPasswordBased(token, this.key);
        } catch (JsonProcessingException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                 IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthenticatedUser validateToken(String token) {
        try {
            String rawDecryptedToken = AESUtil.decryptPasswordBased(token, key);
            Token parsedToken = TOKEN_MAPPER.readValue(rawDecryptedToken, Token.class);

            if (parsedToken.getExpirationDate().isAfter(LocalDateTime.now())) {
                return new AuthenticatedUser(parsedToken.getUsername(), parsedToken.getRole());
            } else return null;
        } catch (JsonProcessingException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                 IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
