package com.imt.API_authentification.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imt.API_authentification.controller.dto.input.RefreshTokenHttpRequestDTO;
import com.imt.API_authentification.controller.dto.input.TokenHttpRequestDTO;
import com.imt.API_authentification.controller.dto.input.UserHttpDTO;
import com.imt.API_authentification.persistence.dao.RefreshTokenMongoDAO;
import com.imt.API_authentification.persistence.dao.RevokedTokenMongoDAO;
import com.imt.API_authentification.persistence.dao.UserMongoDAO;
import com.imt.API_authentification.persistence.dto.RefreshTokenMongoDTO;
import com.imt.API_authentification.persistence.dto.Role;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
        "app.security.secret=integration-test-secret",
        "app.security.salt=integration-test-salt",
        "app.security.refresh-token-ttl-days=30"
})
@AutoConfigureMockMvc
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserMongoDAO userMongoDAO;

    @MockBean
    private RevokedTokenMongoDAO revokedTokenMongoDAO;

    @MockBean
    private RefreshTokenMongoDAO refreshTokenMongoDAO;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldRegisterAndLoginAndVerifyToken() throws Exception {
        // 1. Register
        UserHttpDTO registerDto = new UserHttpDTO(null, null);
        registerDto.setUsername("integrationUser");
        registerDto.setPassword("integrationPass");

        when(userMongoDAO.save(any(UserMongoDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MvcResult registerResult = mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String token = objectMapper.readTree(registerResult.getResponse().getContentAsString()).get("token").asText();
        assertNotNull(token);

        // Capture the actually-saved (BCrypt-hashed) user so the login step compares against
        // a real hash rather than the raw plaintext password.
        ArgumentCaptor<UserMongoDTO> savedUserCaptor = ArgumentCaptor.forClass(UserMongoDTO.class);
        verify(userMongoDAO).save(savedUserCaptor.capture());
        UserMongoDTO savedUser = savedUserCaptor.getValue();

        // 2. Login
        when(userMongoDAO.findByUsername("integrationUser")).thenReturn(savedUser);

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String loginToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
        assertNotNull(loginToken);

        // 3. Verify Token
        TokenHttpRequestDTO verifyDto = new TokenHttpRequestDTO(null);
        verifyDto.setToken(loginToken);

        mockMvc.perform(post("/user/verify-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("integrationUser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldLoginAndRefreshAccessTokenAndRejectReuse() throws Exception {
        UserHttpDTO loginDto = new UserHttpDTO(null, null);
        loginDto.setUsername("refreshUser");
        loginDto.setPassword("integrationPass");

        UserMongoDTO savedUser = new UserMongoDTO(
                UUID.randomUUID(), "refreshUser",
                new BCryptPasswordEncoder().encode("integrationPass"),
                Role.USER);
        when(userMongoDAO.findByUsername("refreshUser")).thenReturn(savedUser);

        // In-memory stand-in for the refresh_tokens collection so save/findById/deleteById
        // behave consistently across the login -> refresh -> reuse steps below.
        Map<String, RefreshTokenMongoDTO> store = new HashMap<>();
        when(refreshTokenMongoDAO.save(any(RefreshTokenMongoDTO.class))).thenAnswer(invocation -> {
            RefreshTokenMongoDTO dto = invocation.getArgument(0);
            store.put(dto.getTokenHash(), dto);
            return dto;
        });
        when(refreshTokenMongoDAO.findById(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(store.get(invocation.getArgument(0))));

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String refreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("refreshToken").asText();
        assertNotNull(refreshToken);

        RefreshTokenHttpRequestDTO refreshDto = new RefreshTokenHttpRequestDTO(refreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/user/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String newAccessToken = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .get("token").asText();

        TokenHttpRequestDTO verifyDto = new TokenHttpRequestDTO(null);
        verifyDto.setToken(newAccessToken);

        mockMvc.perform(post("/user/verify-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("refreshUser"));

        // The old refresh token was rotated away by the first call above — reusing it must fail.
        mockMvc.perform(post("/user/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshDto)))
                .andExpect(status().isBadRequest());
    }
}
