package com.imt.API_authentification.controller.dto.output;

import lombok.Data;

@Data
public class LoginHttpDTO {
    private String token;
    private String refreshToken;

    public LoginHttpDTO(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
