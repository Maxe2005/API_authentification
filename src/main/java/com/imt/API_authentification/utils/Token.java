package com.imt.API_authentification.utils;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Token {
    private final String username;
    private final LocalDateTime expirationDate;

    public Token(String username) {
        this.username = username;
        this.expirationDate = LocalDateTime.now().plusHours(1);
    }
}
