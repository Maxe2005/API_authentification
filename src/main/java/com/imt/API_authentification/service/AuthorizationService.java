package com.imt.API_authentification.service;

import com.imt.API_authentification.exception.InsufficientRoleException;
import com.imt.API_authentification.exception.TokenInvalidException;
import com.imt.API_authentification.persistence.dto.Role;
import com.imt.API_authentification.utils.AuthHandler;
import com.imt.API_authentification.utils.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final AuthHandler authHandler;

    public AuthenticatedUser requireValidToken(String token) throws TokenInvalidException {
        AuthenticatedUser user = authHandler.validateToken(token);
        if (user == null) throw new TokenInvalidException("Invalid token");
        return user;
    }

    public AuthenticatedUser requireAdmin(String token) throws TokenInvalidException, InsufficientRoleException {
        AuthenticatedUser user = requireValidToken(token);
        if (user.role() != Role.ADMIN) throw new InsufficientRoleException("Admin role required");
        return user;
    }
}
