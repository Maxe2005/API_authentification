package com.imt.API_authentification.controller.dto.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenHttpRequestDTO {
    private String refreshToken;

    public RefreshTokenHttpRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
