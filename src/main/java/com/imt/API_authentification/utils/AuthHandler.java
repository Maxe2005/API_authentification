package com.imt.API_authentification.utils;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;

public class AuthHandler {
    private static final String secret = "super_secret_key_that_no_one_should_know";
    private static final String salt = "123456789salt987654321";
    private static final SecretKey key;

    static {
        try {
            key = AESUtil.getKeyFromPassword(secret,salt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateToken(String username) {
        try {
            String token = new Token(username).toString();
            return AESUtil.encryptPasswordBased(token, key);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static String validateToken(String token) {
        try {
            String rawDecryptedToken = AESUtil.decryptPasswordBased(token, key);
            Token parsedToken = Token.fromString(rawDecryptedToken);

            if(parsedToken.getExpirationDate().isAfter(LocalDateTime.now())) {
                return parsedToken.getUsername();
            }
            else return null;
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
