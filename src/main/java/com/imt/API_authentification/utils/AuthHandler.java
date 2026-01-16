package com.imt.API_authentification.utils;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class AuthHandler {
    private static final String secret = "super_secret_key_that_no_one_should_know";
    private static final String salt = "123456789salt987654321";
    private static final GCMParameterSpec spec = AESUtil.generateIv();
    private static final SecretKey key;

    static {
        try {
            key = AESUtil.getKeyFromPassword(secret,salt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateToken(String username) throws InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String token = new Token(username).toString();
        return AESUtil.encryptPasswordBased(token, key, spec);
    }

    public static boolean validateToken(String token) {
        return true;
    }
}
