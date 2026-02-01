package com.imt.API_authentification.utils;

import lombok.Data;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class Token {
    private final String username;
    private final LocalDateTime expirationDate;

    public Token(String username) {
        this.username = username;
        this.expirationDate = LocalDateTime.now().plusHours(1);
    }
    public Token(String username, String expirationDate) {
        this.username = username;
        if (expirationDate != null) {
            this.expirationDate = LocalDateTime.parse(expirationDate);
        } else {
            this.expirationDate = LocalDateTime.now().plusHours(1);
        };
    }

    public static Token fromString(String token) {
        Pattern pattern = Pattern.compile("username=(.*?), expirationDate=(.*?)\\)");
        Matcher matcher = pattern.matcher(token);

        if (matcher.find()) {
            return new Token(matcher.group(1), matcher.group(2));
        }
        throw new IllegalArgumentException("Format de chaîne invalide");
    }

}
