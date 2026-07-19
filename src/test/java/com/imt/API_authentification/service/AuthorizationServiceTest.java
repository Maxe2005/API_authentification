package com.imt.API_authentification.service;

import com.imt.API_authentification.exception.InsufficientRoleException;
import com.imt.API_authentification.exception.TokenInvalidException;
import com.imt.API_authentification.persistence.dto.Role;
import com.imt.API_authentification.utils.AuthHandler;
import com.imt.API_authentification.utils.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
