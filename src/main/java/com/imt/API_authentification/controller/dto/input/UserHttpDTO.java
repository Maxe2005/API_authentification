package com.imt.API_authentification.controller.dto.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserHttpDTO {
    private String username;
    private String password;

    public UserHttpDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
