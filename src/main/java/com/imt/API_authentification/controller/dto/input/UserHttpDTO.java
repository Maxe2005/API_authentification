package com.imt.API_authentification.controller.dto.input;

import lombok.Getter;

@Getter
public class UserHttpDTO {
    private final String username;
    private final String password;

    public UserHttpDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
