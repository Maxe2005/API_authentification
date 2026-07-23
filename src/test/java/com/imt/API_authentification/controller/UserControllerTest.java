package com.imt.API_authentification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imt.API_authentification.controller.dto.input.AdminRegisterHttpDTO;
import com.imt.API_authentification.controller.dto.input.LogoutHttpRequestDTO;
import com.imt.API_authentification.controller.dto.input.RefreshTokenHttpRequestDTO;
import com.imt.API_authentification.controller.dto.input.TokenHttpRequestDTO;
import com.imt.API_authentification.controller.dto.input.UserHttpDTO;
import com.imt.API_authentification.exception.InsufficientRoleException;
import com.imt.API_authentification.exception.TokenInvalidException;
import com.imt.API_authentification.persistence.dto.Role;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import com.imt.API_authentification.service.AuthorizationService;
import com.imt.API_authentification.service.TokenPair;
import com.imt.API_authentification.service.UserService;
import com.imt.API_authentification.utils.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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
    private AuthorizationService authorizationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String validToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        validToken = "valid-token-for-tests";
        adminToken = "admin-token-for-tests";

        when(authorizationService.issueTokenPair(anyString(), any(Role.class)))
                .thenReturn(new TokenPair(validToken, "refresh-token-for-tests"));

        when(authorizationService.requireValidToken(validToken))
                .thenReturn(new AuthenticatedUser("testuser", Role.USER));
        when(authorizationService.requireValidToken("invalidtoken"))
                .thenThrow(new TokenInvalidException("Invalid token"));

        when(authorizationService.requireAdmin(adminToken))
                .thenReturn(new AuthenticatedUser("adminuser", Role.ADMIN));
        when(authorizationService.requireAdmin(validToken))
                .thenThrow(new InsufficientRoleException("Admin role required"));

        org.mockito.Mockito.doThrow(new TokenInvalidException("Invalid token"))
                .when(authorizationService).logout(eq("invalidtoken"), any());
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
    void register_shouldReturnBadRequest_whenUsernameTooShort() throws Exception {
        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
        userHttpDTO.setUsername("ab");
        userHttpDTO.setPassword("password");

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userHttpDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturnBadRequest_whenPasswordTooShort() throws Exception {
        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
        userHttpDTO.setUsername("testuser");
        userHttpDTO.setPassword("short1");

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userHttpDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturnBadRequest_whenUsernameHasInvalidCharacters() throws Exception {
        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
        userHttpDTO.setUsername("invalid user!");
        userHttpDTO.setPassword("password");

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userHttpDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logout_shouldReturnOk_whenTokenIsValid() throws Exception {
        LogoutHttpRequestDTO logoutHttpRequestDTO = new LogoutHttpRequestDTO(null, null);
        logoutHttpRequestDTO.setToken(validToken);

        mockMvc.perform(post("/user/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutHttpRequestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void logout_shouldReturnBadRequest_whenTokenIsInvalid() throws Exception {
        LogoutHttpRequestDTO logoutHttpRequestDTO = new LogoutHttpRequestDTO(null, null);
        logoutHttpRequestDTO.setToken("invalidtoken");

        mockMvc.perform(post("/user/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutHttpRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logout_shouldRevokeRefreshToken_whenProvided() throws Exception {
        LogoutHttpRequestDTO logoutHttpRequestDTO = new LogoutHttpRequestDTO(null, null);
        logoutHttpRequestDTO.setToken(validToken);
        logoutHttpRequestDTO.setRefreshToken("some-refresh-token");

        mockMvc.perform(post("/user/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutHttpRequestDTO)))
                .andExpect(status().isOk());

        org.mockito.Mockito.verify(authorizationService).logout(validToken, "some-refresh-token");
    }

    @Test
    void refreshToken_shouldReturnOk_whenRefreshTokenIsValid() throws Exception {
        when(authorizationService.refresh("valid-refresh")).thenReturn(new TokenPair("new-access-token", "new-refresh-token"));

        RefreshTokenHttpRequestDTO dto = new RefreshTokenHttpRequestDTO("valid-refresh");

        mockMvc.perform(post("/user/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void refreshToken_shouldReturnBadRequest_whenRefreshTokenIsInvalid() throws Exception {
        when(authorizationService.refresh("invalid-refresh"))
                .thenThrow(new TokenInvalidException("Invalid refresh token"));

        RefreshTokenHttpRequestDTO dto = new RefreshTokenHttpRequestDTO("invalid-refresh");

        mockMvc.perform(post("/user/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
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
        when(authorizationService.issueTokenPair("newadmin", Role.ADMIN))
                .thenReturn(new TokenPair("new-admin-token", "new-admin-refresh-token"));

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
