package com.imt.API_authentification.controller.dto.output;


import com.imt.API_authentification.persistence.dto.Role;
import lombok.Data;

@Data
public class TokenHttpResponseDTO {
    private final String username;
    private final Role role;

    public TokenHttpResponseDTO(String username, Role role) {
        this.username = username;
        this.role = role;
    }
}
