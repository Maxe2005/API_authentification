package com.imt.API_authentification.controller.dto.input;

import lombok.Getter;

@Getter
public class TokenHttpDTO {
    private final String username;
    private final String token;

    public TokenHttpDTO(String username, String token) {
        this.username = username;
        this.token = token;
    }
}