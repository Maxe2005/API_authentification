package com.imt.API_authentification.utils;

import com.imt.API_authentification.persistence.dto.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthHandlerTest {

    private AuthHandler authHandler;

    @BeforeEach
    void setUp() throws Exception {
        SecurityProperties properties = new SecurityProperties();
        properties.setSecret("unit-test-secret");
        properties.setSalt("unit-test-salt");
        authHandler = new AuthHandler(properties);
    }

    @Test
    void generateThenValidateToken_roundTripsUsernameAndRole() {
        String token = authHandler.generateToken("alice", Role.ADMIN);

        AuthenticatedUser user = authHandler.validateToken(token);

        assertEquals("alice", user.username());
        assertEquals(Role.ADMIN, user.role());
    }

    @Test
    void validateToken_garbageInput_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () -> authHandler.validateToken("not-a-valid-token"));
    }
}
