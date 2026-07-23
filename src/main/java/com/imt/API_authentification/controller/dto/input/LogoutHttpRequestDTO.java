package com.imt.API_authentification.controller.dto.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutHttpRequestDTO {
    private String token;
    private String refreshToken;

    public LogoutHttpRequestDTO(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
