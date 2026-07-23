package com.imt.API_authentification.service;

import com.imt.API_authentification.exception.TokenInvalidException;
import com.imt.API_authentification.persistence.dao.RefreshTokenMongoDAO;
import com.imt.API_authentification.persistence.dto.RefreshTokenMongoDTO;
import com.imt.API_authentification.utils.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenMongoDAO refreshTokenMongoDAO;

    @Mock
    private SecurityProperties securityProperties;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void issue_shouldSaveHashedTokenWithMatchingUsernameAndExpiration() {
        when(securityProperties.getRefreshTokenTtlDays()).thenReturn(30);

        String rawToken = refreshTokenService.issue("testuser");

        assertNotNull(rawToken);
        ArgumentCaptor<RefreshTokenMongoDTO> captor = ArgumentCaptor.forClass(RefreshTokenMongoDTO.class);
        verify(refreshTokenMongoDAO).save(captor.capture());

        RefreshTokenMongoDTO saved = captor.getValue();
        assertEquals("testuser", saved.getUsername());
        assertTrue(saved.getTokenHash().length() == 64);
        assertTrue(saved.getExpiresAt().isAfter(Instant.now().plus(29, ChronoUnit.DAYS)));
        assertTrue(saved.getExpiresAt().isBefore(Instant.now().plus(31, ChronoUnit.DAYS)));
    }

    @Test
    void rotate_shouldReturnNewTokenAndRevokeOld_whenTokenIsValid() throws TokenInvalidException {
        RefreshTokenMongoDTO existing = new RefreshTokenMongoDTO("hash", "testuser", Instant.now().plusSeconds(3600));
        when(refreshTokenMongoDAO.findById(anyString())).thenReturn(Optional.of(existing));
        when(securityProperties.getRefreshTokenTtlDays()).thenReturn(30);

        RefreshTokenService.RefreshResult result = refreshTokenService.rotate("raw-token");

        assertEquals("testuser", result.username());
        assertNotNull(result.refreshToken());
        assertTrue(existing.isRevoked());
        verify(refreshTokenMongoDAO).save(existing);
    }

    @Test
    void rotate_shouldThrow_whenTokenDoesNotExist() {
        when(refreshTokenMongoDAO.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(TokenInvalidException.class, () -> refreshTokenService.rotate("unknown-token"));
    }

    @Test
    void rotate_shouldThrow_whenTokenIsExpired() {
        RefreshTokenMongoDTO expired = new RefreshTokenMongoDTO("hash", "testuser", Instant.now().minusSeconds(1));
        when(refreshTokenMongoDAO.findById(anyString())).thenReturn(Optional.of(expired));

        assertThrows(TokenInvalidException.class, () -> refreshTokenService.rotate("raw-token"));
    }

    @Test
    void rotate_shouldRevokeAllUserTokensAndThrow_whenTokenWasAlreadyUsed() {
        RefreshTokenMongoDTO reused = new RefreshTokenMongoDTO("hash", "testuser", Instant.now().plusSeconds(3600));
        reused.setRevoked(true);
        when(refreshTokenMongoDAO.findById(anyString())).thenReturn(Optional.of(reused));

        assertThrows(TokenInvalidException.class, () -> refreshTokenService.rotate("raw-token"));

        verify(refreshTokenMongoDAO).deleteByUsername("testuser");
    }

    @Test
    void revoke_shouldDeleteByHash() {
        refreshTokenService.revoke("raw-token");

        verify(refreshTokenMongoDAO).deleteById(anyString());
    }

    @Test
    void revokeAllForUser_shouldDeleteByUsername() {
        refreshTokenService.revokeAllForUser("testuser");

        verify(refreshTokenMongoDAO).deleteByUsername("testuser");
    }

    @Test
    void issue_shouldUseSameHash_asRotateLookup() throws TokenInvalidException {
        when(securityProperties.getRefreshTokenTtlDays()).thenReturn(30);

        String rawToken = refreshTokenService.issue("testuser");

        ArgumentCaptor<RefreshTokenMongoDTO> captor = ArgumentCaptor.forClass(RefreshTokenMongoDTO.class);
        verify(refreshTokenMongoDAO).save(captor.capture());
        String hashUsedOnIssue = captor.getValue().getTokenHash();

        when(refreshTokenMongoDAO.findById(hashUsedOnIssue)).thenReturn(Optional.of(captor.getValue()));

        refreshTokenService.rotate(rawToken);

        verify(refreshTokenMongoDAO).findById(hashUsedOnIssue);
    }
}
