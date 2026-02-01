package com.imt.API_authentification.controller.dto.output;


import lombok.Data;

@Data
public class TokenHttpResponseDTO {
    private final String username;

    public TokenHttpResponseDTO(String username) {
        this.username = username;
    }
}
