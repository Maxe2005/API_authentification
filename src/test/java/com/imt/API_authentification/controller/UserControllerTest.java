package com.imt.API_authentification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imt.API_authentification.controller.dto.input.AdminRegisterHttpDTO;
import com.imt.API_authentification.controller.dto.input.TokenHttpRequestDTO;
import com.imt.API_authentification.controller.dto.input.UserHttpDTO;
import com.imt.API_authentification.exception.InsufficientRoleException;
import com.imt.API_authentification.exception.TokenInvalidException;
import com.imt.API_authentification.persistence.dto.Role;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import com.imt.API_authentification.service.AuthorizationService;
import com.imt.API_authentification.service.UserService;
import com.imt.API_authentification.utils.AuthHandler;
import com.imt.API_authentification.utils.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthHandler authHandler;

    @MockBean
    private AuthorizationService authorizationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String validToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        validToken = "valid-token-for-tests";
        adminToken = "admin-token-for-tests";

        when(authHandler.generateToken("testuser", Role.USER)).thenReturn(validToken);

        when(authorizationService.requireValidToken(validToken))
                .thenReturn(new AuthenticatedUser("testuser", Role.USER));
        when(authorizationService.requireValidToken("invalidtoken"))
                .thenThrow(new TokenInvalidException("Invalid token"));

        when(authorizationService.requireAdmin(adminToken))
                .thenReturn(new AuthenticatedUser("adminuser", Role.ADMIN));
        when(authorizationService.requireAdmin(validToken))
                .thenThrow(new InsufficientRoleException("Admin role required"));
    }

    @Test
    void register_shouldReturnOk_whenRegistrationIsSuccessful() throws Exception {
        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
        userHttpDTO.setUsername("testuser");
        userHttpDTO.setPassword("password");

        when(userService.register(anyString(), anyString(), eq(Role.USER))).thenReturn(true);

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userHttpDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void register_shouldAlwaysUseUserRole_regardlessOfInput() throws Exception {
        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
        userHttpDTO.setUsername("testuser");
        userHttpDTO.setPassword("password");

        when(userService.register(anyString(), anyString(), eq(Role.USER))).thenReturn(true);

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userHttpDTO)))
                .andExpect(status().isOk());

        org.mockito.Mockito.verify(userService).register("testuser", "password", Role.USER);
    }

    @Test
    void register_shouldReturnBadRequest_whenRegistrationFails() throws Exception {
        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
        userHttpDTO.setUsername("testuser");
        userHttpDTO.setPassword("password");

        when(userService.register(anyString(), anyString(), eq(Role.USER))).thenReturn(false);

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userHttpDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnOk_whenLoginIsSuccessful() throws Exception {
        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
        userHttpDTO.setUsername("testuser");
        userHttpDTO.setPassword("password");

        UserMongoDTO userMongoDTO = new UserMongoDTO(UUID.randomUUID(), "testuser", "hashed", Role.USER);

        when(userService.getUser("testuser")).thenReturn(userMongoDTO);
        when(userService.checkPassword(userMongoDTO, "password")).thenReturn(true);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userHttpDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
        userHttpDTO.setUsername("testuser");
        userHttpDTO.setPassword("password");

        when(userService.getUser("testuser")).thenReturn(null);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userHttpDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnBadRequest_whenPasswordIsIncorrect() throws Exception {
        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
        userHttpDTO.setUsername("testuser");
        userHttpDTO.setPassword("wrongpassword");

        UserMongoDTO userMongoDTO = new UserMongoDTO(UUID.randomUUID(), "testuser", "hashed", Role.USER);

        when(userService.getUser("testuser")).thenReturn(userMongoDTO);
        when(userService.checkPassword(userMongoDTO, "wrongpassword")).thenReturn(false);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userHttpDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyToken_shouldReturnOk_whenTokenIsValid() throws Exception {
        TokenHttpRequestDTO tokenHttpRequestDTO = new TokenHttpRequestDTO(null);
        tokenHttpRequestDTO.setToken(validToken);

        mockMvc.perform(post("/user/verify-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenHttpRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void verifyToken_shouldReturnBadRequest_whenTokenIsInvalid() throws Exception {
        TokenHttpRequestDTO tokenHttpRequestDTO = new TokenHttpRequestDTO(null);
        tokenHttpRequestDTO.setToken("invalidtoken");

        mockMvc.perform(post("/user/verify-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenHttpRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturnOk_whenTokenIsValid() throws Exception {
        TokenHttpRequestDTO tokenHttpRequestDTO = new TokenHttpRequestDTO(null);
        tokenHttpRequestDTO.setToken(validToken);

        mockMvc.perform(post("/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenHttpRequestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturnBadRequest_whenTokenIsInvalid() throws Exception {
        TokenHttpRequestDTO tokenHttpRequestDTO = new TokenHttpRequestDTO(null);
        tokenHttpRequestDTO.setToken("invalidtoken");

        mockMvc.perform(post("/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenHttpRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminRegister_shouldReturnOk_whenCallerIsAdmin() throws Exception {
        AdminRegisterHttpDTO dto = new AdminRegisterHttpDTO(adminToken, "newadmin", "password", Role.ADMIN);

        when(userService.register("newadmin", "password", Role.ADMIN)).thenReturn(true);
        when(authHandler.generateToken("newadmin", Role.ADMIN)).thenReturn("new-admin-token");

        mockMvc.perform(post("/user/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void adminRegister_shouldReturnForbidden_whenCallerIsNotAdmin() throws Exception {
        AdminRegisterHttpDTO dto = new AdminRegisterHttpDTO(validToken, "newadmin", "password", Role.ADMIN);

        mockMvc.perform(post("/user/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRegister_shouldReturnBadRequest_whenTokenInvalid() throws Exception {
        AdminRegisterHttpDTO dto = new AdminRegisterHttpDTO("invalidtoken", "newadmin", "password", Role.ADMIN);

        mockMvc.perform(post("/user/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminDelete_shouldReturnNoContent_whenCallerIsAdmin() throws Exception {
        TokenHttpRequestDTO dto = new TokenHttpRequestDTO(adminToken);

        when(userService.getUser("someuser")).thenReturn(
                new UserMongoDTO(UUID.randomUUID(), "someuser", "hashed", Role.USER));

        mockMvc.perform(post("/user/admin/delete/someuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminDelete_shouldReturnForbidden_whenCallerIsNotAdmin() throws Exception {
        TokenHttpRequestDTO dto = new TokenHttpRequestDTO(validToken);

        mockMvc.perform(post("/user/admin/delete/someuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
