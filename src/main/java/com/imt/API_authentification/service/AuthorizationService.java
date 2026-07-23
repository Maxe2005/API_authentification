package com.imt.API_authentification.service;

import com.imt.API_authentification.exception.InsufficientRoleException;
import com.imt.API_authentification.exception.TokenInvalidException;
import com.imt.API_authentification.persistence.dto.Role;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import com.imt.API_authentification.utils.AuthHandler;
import com.imt.API_authentification.utils.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final AuthHandler authHandler;
    private final TokenRevocationService tokenRevocationService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public AuthenticatedUser requireValidToken(String token) throws TokenInvalidException {
        AuthenticatedUser user = authHandler.validateToken(token);
        if (user == null) throw new TokenInvalidException("Invalid token");
        if (tokenRevocationService.isRevoked(token)) throw new TokenInvalidException("Token has been revoked");
        return user;
    }

    public AuthenticatedUser requireAdmin(String token) throws TokenInvalidException, InsufficientRoleException {
        AuthenticatedUser user = requireValidToken(token);
        if (user.role() != Role.ADMIN) throw new InsufficientRoleException("Admin role required");
        return user;
    }

    public void logout(String token) throws TokenInvalidException {
        requireValidToken(token);
        tokenRevocationService.revoke(token);
    }

    public void logout(String token, String refreshToken) throws TokenInvalidException {
        logout(token);
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revoke(refreshToken);
        }
    }

    public TokenPair issueTokenPair(String username, Role role) {
        String accessToken = authHandler.generateToken(username, role);
        String refreshToken = refreshTokenService.issue(username);
        return new TokenPair(accessToken, refreshToken);
    }

    public TokenPair refresh(String refreshToken) throws TokenInvalidException {
        RefreshTokenService.RefreshResult rotated = refreshTokenService.rotate(refreshToken);

        UserMongoDTO user = userService.getUser(rotated.username());
        if (user == null) {
            refreshTokenService.revoke(rotated.refreshToken());
            throw new TokenInvalidException("User no longer exists");
        }

        String accessToken = authHandler.generateToken(user.getUsername(), user.getRole());
        return new TokenPair(accessToken, rotated.refreshToken());
    }
}
