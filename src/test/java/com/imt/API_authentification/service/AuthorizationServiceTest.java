package com.imt.API_authentification.service;

import com.imt.API_authentification.exception.InsufficientRoleException;
import com.imt.API_authentification.exception.TokenInvalidException;
import com.imt.API_authentification.persistence.dto.Role;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import com.imt.API_authentification.utils.AuthHandler;
import com.imt.API_authentification.utils.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private AuthHandler authHandler;

    @Mock
    private TokenRevocationService tokenRevocationService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Test
    void requireValidToken_shouldReturnUser_whenTokenIsValidAndNotRevoked() throws TokenInvalidException {
        when(authHandler.validateToken("token")).thenReturn(new AuthenticatedUser("testuser", Role.USER));
        when(tokenRevocationService.isRevoked("token")).thenReturn(false);

        AuthenticatedUser user = authorizationService.requireValidToken("token");

        assertEquals("testuser", user.username());
    }

    @Test
    void requireValidToken_shouldThrow_whenTokenIsInvalid() {
        when(authHandler.validateToken("token")).thenReturn(null);

        assertThrows(TokenInvalidException.class, () -> authorizationService.requireValidToken("token"));
    }

    @Test
    void requireValidToken_shouldThrow_whenTokenIsRevoked() {
        when(authHandler.validateToken("token")).thenReturn(new AuthenticatedUser("testuser", Role.USER));
        when(tokenRevocationService.isRevoked("token")).thenReturn(true);

        assertThrows(TokenInvalidException.class, () -> authorizationService.requireValidToken("token"));
    }

    @Test
    void requireAdmin_shouldReturnUser_whenRoleIsAdmin() throws TokenInvalidException, InsufficientRoleException {
        when(authHandler.validateToken("token")).thenReturn(new AuthenticatedUser("adminuser", Role.ADMIN));
        when(tokenRevocationService.isRevoked("token")).thenReturn(false);

        AuthenticatedUser user = authorizationService.requireAdmin("token");

        assertEquals(Role.ADMIN, user.role());
    }

    @Test
    void requireAdmin_shouldThrow_whenRoleIsNotAdmin() {
        when(authHandler.validateToken("token")).thenReturn(new AuthenticatedUser("testuser", Role.USER));
        when(tokenRevocationService.isRevoked("token")).thenReturn(false);

        assertThrows(InsufficientRoleException.class, () -> authorizationService.requireAdmin("token"));
    }

    @Test
    void logout_shouldRevokeToken_whenTokenIsValid() throws TokenInvalidException {
        when(authHandler.validateToken("token")).thenReturn(new AuthenticatedUser("testuser", Role.USER));
        when(tokenRevocationService.isRevoked("token")).thenReturn(false);

        authorizationService.logout("token");

        verify(tokenRevocationService).revoke("token");
    }

    @Test
    void logout_shouldThrowAndNotRevoke_whenTokenIsInvalid() {
        when(authHandler.validateToken("token")).thenReturn(null);

        assertThrows(TokenInvalidException.class, () -> authorizationService.logout("token"));

        verify(tokenRevocationService, never()).revoke("token");
    }

    @Test
    void logoutWithRefreshToken_shouldRevokeBoth_whenTokenIsValid() throws TokenInvalidException {
        when(authHandler.validateToken("token")).thenReturn(new AuthenticatedUser("testuser", Role.USER));
        when(tokenRevocationService.isRevoked("token")).thenReturn(false);

        authorizationService.logout("token", "refresh-token");

        verify(tokenRevocationService).revoke("token");
        verify(refreshTokenService).revoke("refresh-token");
    }

    @Test
    void logoutWithRefreshToken_shouldNotRevokeRefreshToken_whenNotProvided() throws TokenInvalidException {
        when(authHandler.validateToken("token")).thenReturn(new AuthenticatedUser("testuser", Role.USER));
        when(tokenRevocationService.isRevoked("token")).thenReturn(false);

        authorizationService.logout("token", null);

        verify(tokenRevocationService).revoke("token");
        verify(refreshTokenService, never()).revoke(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void issueTokenPair_shouldReturnAccessAndRefreshTokens() {
        when(authHandler.generateToken("testuser", Role.USER)).thenReturn("access-token");
        when(refreshTokenService.issue("testuser")).thenReturn("refresh-token");

        TokenPair tokens = authorizationService.issueTokenPair("testuser", Role.USER);

        assertEquals("access-token", tokens.accessToken());
        assertEquals("refresh-token", tokens.refreshToken());
    }

    @Test
    void refresh_shouldReturnNewTokenPair_whenRefreshTokenIsValidAndUserExists() throws TokenInvalidException {
        when(refreshTokenService.rotate("old-refresh"))
                .thenReturn(new RefreshTokenService.RefreshResult("testuser", "new-refresh"));
        UserMongoDTO user = new UserMongoDTO(UUID.randomUUID(), "testuser", "hashed", Role.USER);
        when(userService.getUser("testuser")).thenReturn(user);
        when(authHandler.generateToken("testuser", Role.USER)).thenReturn("new-access");

        TokenPair tokens = authorizationService.refresh("old-refresh");

        assertEquals("new-access", tokens.accessToken());
        assertEquals("new-refresh", tokens.refreshToken());
    }

    @Test
    void refresh_shouldThrowAndRevokeNewToken_whenUserNoLongerExists() throws TokenInvalidException {
        when(refreshTokenService.rotate("old-refresh"))
                .thenReturn(new RefreshTokenService.RefreshResult("ghost", "new-refresh"));
        when(userService.getUser("ghost")).thenReturn(null);

        assertThrows(TokenInvalidException.class, () -> authorizationService.refresh("old-refresh"));

        verify(refreshTokenService).revoke("new-refresh");
    }

    @Test
    void refresh_shouldPropagate_whenRotateThrows() throws TokenInvalidException {
        when(refreshTokenService.rotate("old-refresh")).thenThrow(new TokenInvalidException("Invalid refresh token"));

        assertThrows(TokenInvalidException.class, () -> authorizationService.refresh("old-refresh"));

        verify(userService, never()).getUser(org.mockito.ArgumentMatchers.anyString());
    }
}
