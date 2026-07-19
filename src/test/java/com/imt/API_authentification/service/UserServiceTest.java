package com.imt.API_authentification.service;

import com.imt.API_authentification.persistence.dao.UserMongoDAO;
import com.imt.API_authentification.persistence.dto.Role;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMongoDAO userMongoDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getUser_shouldReturnUser_whenUserExists() {
        String username = "testuser";
        UserMongoDTO expectedUser = new UserMongoDTO(UUID.randomUUID(), username, "password", Role.USER);
        when(userMongoDAO.findByUsername(username)).thenReturn(expectedUser);

        UserMongoDTO actualUser = userService.getUser(username);

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void getUser_shouldReturnNull_whenUserDoesNotExist() {
        String username = "testuser";
        when(userMongoDAO.findByUsername(username)).thenReturn(null);

        UserMongoDTO actualUser = userService.getUser(username);

        assertNull(actualUser);
    }

    @Test
    void register_shouldSaveUserAndReturnTrue() {
        String username = "testuser";
        String password = "password";

        boolean result = userService.register(username, password, Role.USER);

        assertTrue(result);
        verify(userMongoDAO).save(any(UserMongoDTO.class));
    }

    @Test
    void register_shouldHashPasswordBeforeSaving() {
        when(passwordEncoder.encode("password")).thenReturn("hashed-value");

        userService.register("testuser", "password", Role.USER);

        ArgumentCaptor<UserMongoDTO> captor = ArgumentCaptor.forClass(UserMongoDTO.class);
        verify(userMongoDAO).save(captor.capture());
        assertEquals("hashed-value", captor.getValue().getPassword());
        assertEquals(Role.USER, captor.getValue().getRole());
    }

    @Test
    void register_shouldReturnFalse_whenUserAlreadyExists() {
        when(userMongoDAO.findByUsername("testuser")).thenReturn(
                new UserMongoDTO(UUID.randomUUID(), "testuser", "hashed", Role.USER));

        boolean result = userService.register("testuser", "password", Role.USER);

        assertFalse(result);
        verify(userMongoDAO, never()).save(any());
    }

    @Test
    void checkPassword_shouldReturnTrue_whenPasswordMatches() {
        UserMongoDTO user = new UserMongoDTO(UUID.randomUUID(), "testuser", "hashed", Role.USER);
        when(passwordEncoder.matches("raw", "hashed")).thenReturn(true);

        assertTrue(userService.checkPassword(user, "raw"));
    }

    @Test
    void checkPassword_shouldReturnFalse_whenPasswordDoesNotMatch() {
        UserMongoDTO user = new UserMongoDTO(UUID.randomUUID(), "testuser", "hashed", Role.USER);
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertFalse(userService.checkPassword(user, "wrong"));
    }

    @Test
    void delete_shouldDeleteUser_whenUserExists() {
        UserMongoDTO user = new UserMongoDTO(UUID.randomUUID(), "testuser", "hashed", Role.USER);
        when(userMongoDAO.findByUsername("testuser")).thenReturn(user);

        userService.delete("testuser");

        verify(userMongoDAO).delete(user);
    }

    @Test
    void delete_shouldNoOpGracefully_whenUserDoesNotExist() {
        when(userMongoDAO.findByUsername("testuser")).thenReturn(null);

        userService.delete("testuser");

        verify(userMongoDAO, never()).delete(any());
    }
}
