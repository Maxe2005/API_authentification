package com.imt.API_authentification.utils;

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

    public String generateToken(String username) {
        try {
            String token = new Token(username).toString();
            return AESUtil.encryptPasswordBased(token, this.key);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public String validateToken(String token) {
        try {
            String rawDecryptedToken = AESUtil.decryptPasswordBased(token, key);
            Token parsedToken = Token.fromString(rawDecryptedToken);

            if (parsedToken.getExpirationDate().isAfter(LocalDateTime.now())) {
                return parsedToken.getUsername();
            } else return null;
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
