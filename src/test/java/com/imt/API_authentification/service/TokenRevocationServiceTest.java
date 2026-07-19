package com.imt.API_authentification.service;

import com.imt.API_authentification.persistence.dao.RevokedTokenMongoDAO;
import com.imt.API_authentification.persistence.dto.RevokedTokenMongoDTO;
import com.imt.API_authentification.utils.AuthHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenRevocationServiceTest {

    @Mock
    private RevokedTokenMongoDAO revokedTokenMongoDAO;

    @Mock
    private AuthHandler authHandler;

    @InjectMocks
    private TokenRevocationService tokenRevocationService;

    @Test
    void revoke_shouldSaveHashedTokenWithMatchingExpiration() {
        when(authHandler.getExpirationDate("some-token")).thenReturn(LocalDateTime.now().plusHours(1));

        tokenRevocationService.revoke("some-token");

        ArgumentCaptor<RevokedTokenMongoDTO> captor = ArgumentCaptor.forClass(RevokedTokenMongoDTO.class);
        verify(revokedTokenMongoDAO).save(captor.capture());
        assertFalse(captor.getValue().getTokenHash().isBlank());
        assertTrue(captor.getValue().getTokenHash().length() == 64);
    }

    @Test
    void isRevoked_shouldReturnTrue_whenTokenWasRevoked() {
        when(revokedTokenMongoDAO.existsById(anyString())).thenReturn(true);

        assertTrue(tokenRevocationService.isRevoked("some-token"));
    }

    @Test
    void isRevoked_shouldReturnFalse_whenTokenWasNotRevoked() {
        when(revokedTokenMongoDAO.existsById(anyString())).thenReturn(false);

        assertFalse(tokenRevocationService.isRevoked("some-token"));
    }

    @Test
    void isRevoked_shouldUseSameHash_asRevoke() {
        when(authHandler.getExpirationDate("some-token")).thenReturn(LocalDateTime.now().plusHours(1));
        tokenRevocationService.revoke("some-token");

        ArgumentCaptor<RevokedTokenMongoDTO> captor = ArgumentCaptor.forClass(RevokedTokenMongoDTO.class);
        verify(revokedTokenMongoDAO).save(captor.capture());
        String hashUsedOnRevoke = captor.getValue().getTokenHash();

        when(revokedTokenMongoDAO.existsById(hashUsedOnRevoke)).thenReturn(true);
        assertTrue(tokenRevocationService.isRevoked("some-token"));
        verify(revokedTokenMongoDAO).existsById(hashUsedOnRevoke);
    }
}
