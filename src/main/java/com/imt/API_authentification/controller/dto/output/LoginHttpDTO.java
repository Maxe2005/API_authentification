package com.imt.API_authentification.controller.dto.output;

import lombok.Data;

@Data
public class LoginHttpDTO {
    private String token;

    public LoginHttpDTO(String token) {
        this.token = token;
    }
}
