package com.imt.API_authentification.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.imt.API_authentification.persistence.dto.Role;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Token {
    private final String username;
    private final Role role;
    private final LocalDateTime expirationDate;

    public Token(String username, Role role) {
        this.username = username;
        this.role = role;
        this.expirationDate = LocalDateTime.now().plusHours(1);
    }

    @JsonCreator
    public Token(@JsonProperty("username") String username,
                 @JsonProperty("role") Role role,
                 @JsonProperty("expirationDate") LocalDateTime expirationDate) {
        this.username = username;
        this.role = role;
        this.expirationDate = expirationDate != null ? expirationDate : LocalDateTime.now().plusHours(1);
    }
}
