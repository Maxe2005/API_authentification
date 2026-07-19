package com.imt.API_authentification.controller.dto.input;

import com.imt.API_authentification.persistence.dto.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminRegisterHttpDTO {
    private String token;
    private String username;
    private String password;
    private Role role;

    public AdminRegisterHttpDTO(String token, String username, String password, Role role) {
        this.token = token;
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
