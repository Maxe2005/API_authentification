package com.imt.API_authentification.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.imt.API_authentification.persistence.dto.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TokenTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void constructor_setsUsernameRoleAndOneHourExpiry() {
        Token token = new Token("alice", Role.ADMIN);

        assertEquals("alice", token.getUsername());
        assertEquals(Role.ADMIN, token.getRole());
        assertTrue(token.getExpirationDate().isAfter(LocalDateTime.now().plusMinutes(59)));
        assertTrue(token.getExpirationDate().isBefore(LocalDateTime.now().plusMinutes(61)));
    }

    @Test
    void serializeThenDeserialize_roundTripsAllFieldsRegardlessOfPropertyOrder() throws Exception {
        Token original = new Token("bob", Role.USER);

        String json = mapper.writeValueAsString(original);
        Token roundTripped = mapper.readValue(json, Token.class);

        assertEquals(original.getUsername(), roundTripped.getUsername());
        assertEquals(original.getRole(), roundTripped.getRole());
        assertEquals(original.getExpirationDate(), roundTripped.getExpirationDate());

        // Property order in the JSON must not matter for parsing.
        String reordered = "{\"role\":\"ADMIN\",\"expirationDate\":\"" + original.getExpirationDate() + "\",\"username\":\"carol\"}";
        Token fromReordered = mapper.readValue(reordered, Token.class);
        assertEquals("carol", fromReordered.getUsername());
        assertEquals(Role.ADMIN, fromReordered.getRole());
    }

    @Test
    void fromJson_missingExpirationDate_defaultsToOneHourFromNow() throws Exception {
        String json = "{\"username\":\"dave\",\"role\":\"USER\"}";

        Token token = mapper.readValue(json, Token.class);

        assertEquals("dave", token.getUsername());
        assertEquals(Role.USER, token.getRole());
        assertTrue(token.getExpirationDate().isAfter(LocalDateTime.now().plusMinutes(59)));
    }
}
