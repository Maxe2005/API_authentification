package com.imt.API_authentification.controller.dto.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenHttpRequestDTO {
    private String token;

    public TokenHttpRequestDTO(String token) {
        this.token = token;
    }
}