package com.imt.API_authentification.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imt.API_authentification.controller.dto.input.TokenHttpRequestDTO;
import com.imt.API_authentification.controller.dto.input.UserHttpDTO;
import com.imt.API_authentification.persistence.dao.UserMongoDAO;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration"
})
@AutoConfigureMockMvc
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserMongoDAO userMongoDAO;

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

        // 2. Login
        UserMongoDTO userMongoDTO = new UserMongoDTO(UUID.randomUUID(), "integrationUser", "integrationPass");
        when(userMongoDAO.findByUsername("integrationUser")).thenReturn(userMongoDTO);

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
                .andExpect(jsonPath("$.username").value("integrationUser"));
    }
}
