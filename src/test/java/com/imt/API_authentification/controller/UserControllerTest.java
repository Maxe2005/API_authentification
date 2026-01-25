//package com.imt.API_authentification.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.imt.API_authentification.controller.dto.input.TokenHttpRequestDTO;
//import com.imt.API_authentification.controller.dto.input.UserHttpDTO;
//import com.imt.API_authentification.persistence.dto.UserMongoDTO;
//import com.imt.API_authentification.service.UserService;
//import com.imt.API_authentification.utils.AuthHandler;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(UserController.class)
//class UserControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Mock
//    private UserService userService;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    private static String validToken;
//
//    @BeforeAll
//    static void setUp() {
//        validToken = AuthHandler.generateToken("testuser");
//    }
//
//    @Test
//    void register_shouldReturnOk_whenRegistrationIsSuccessful() throws Exception {
//        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
//        userHttpDTO.setUsername("testuser");
//        userHttpDTO.setPassword("password");
//
//        when(userService.register(anyString(), anyString())).thenReturn(true);
//
//        mockMvc.perform(post("/user")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userHttpDTO)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").exists());
//    }
//
//    @Test
//    void register_shouldReturnBadRequest_whenRegistrationFails() throws Exception {
//        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
//        userHttpDTO.setUsername("testuser");
//        userHttpDTO.setPassword("password");
//
//        when(userService.register(anyString(), anyString())).thenReturn(false);
//
//        mockMvc.perform(post("/user")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userHttpDTO)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void login_shouldReturnOk_whenLoginIsSuccessful() throws Exception {
//        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
//        userHttpDTO.setUsername("testuser");
//        userHttpDTO.setPassword("password");
//
//        UserMongoDTO userMongoDTO = new UserMongoDTO(UUID.randomUUID(), "testuser", "password");
//
//        when(userService.getUser("testuser")).thenReturn(userMongoDTO);
//
//        mockMvc.perform(post("/user/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userHttpDTO)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").exists());
//    }
//
//    @Test
//    void login_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
//        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
//        userHttpDTO.setUsername("testuser");
//        userHttpDTO.setPassword("password");
//
//        when(userService.getUser("testuser")).thenReturn(null);
//
//        mockMvc.perform(post("/user/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userHttpDTO)))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void login_shouldReturnBadRequest_whenPasswordIsIncorrect() throws Exception {
//        UserHttpDTO userHttpDTO = new UserHttpDTO(null, null);
//        userHttpDTO.setUsername("testuser");
//        userHttpDTO.setPassword("wrongpassword");
//
//        UserMongoDTO userMongoDTO = new UserMongoDTO(UUID.randomUUID(), "testuser", "password");
//
//        when(userService.getUser("testuser")).thenReturn(userMongoDTO);
//
//        mockMvc.perform(post("/user/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(userHttpDTO)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void verifyToken_shouldReturnOk_whenTokenIsValid() throws Exception {
//        TokenHttpRequestDTO tokenHttpRequestDTO = new TokenHttpRequestDTO(null);
//        tokenHttpRequestDTO.setToken(validToken);
//
//        mockMvc.perform(post("/user/verify-token")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(tokenHttpRequestDTO)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.username").value("testuser"));
//    }
//
//    @Test
//    void verifyToken_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
//        TokenHttpRequestDTO tokenHttpRequestDTO = new TokenHttpRequestDTO(null);
//        tokenHttpRequestDTO.setToken("invalidtoken");
//
//        mockMvc.perform(post("/user/verify-token")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(tokenHttpRequestDTO)))
//                .andExpect(status().isUnauthorized());
//    }
//}
