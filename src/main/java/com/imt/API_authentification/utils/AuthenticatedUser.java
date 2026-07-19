package com.imt.API_authentification.utils;

import com.imt.API_authentification.persistence.dto.Role;

public record AuthenticatedUser(String username, Role role) {
}
