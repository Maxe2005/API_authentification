package com.imt.API_authentification.config;

import com.imt.API_authentification.persistence.dto.Role;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import com.imt.API_authentification.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultUsersSeederTest {

    @Mock
    private UserService userService;

    private DefaultUsersProperties properties;
    private DefaultUsersSeeder seeder;

    @BeforeEach
    void setUp() {
        properties = new DefaultUsersProperties();
        seeder = new DefaultUsersSeeder(userService, properties);
    }

    @Test
    void run_shouldCreateBothDefaultAccounts_whenNeitherExists() {
        properties.getAdmin().setUsername("admin");
        properties.getAdmin().setPassword("admin-pass");
        properties.getUser().setUsername("user");
        properties.getUser().setPassword("user-pass");

        when(userService.getUser("admin")).thenReturn(null);
        when(userService.getUser("user")).thenReturn(null);

        seeder.run();

        verify(userService).register("admin", "admin-pass", Role.ADMIN);
        verify(userService).register("user", "user-pass", Role.USER);
    }

    @Test
    void run_shouldSkipCreation_whenAccountsAlreadyExist() {
        properties.getAdmin().setUsername("admin");
        properties.getAdmin().setPassword("admin-pass");
        properties.getUser().setUsername("user");
        properties.getUser().setPassword("user-pass");

        when(userService.getUser("admin")).thenReturn(
                new UserMongoDTO(UUID.randomUUID(), "admin", "hashed", Role.ADMIN));
        when(userService.getUser("user")).thenReturn(
                new UserMongoDTO(UUID.randomUUID(), "user", "hashed", Role.USER));

        seeder.run();

        verify(userService, never()).register("admin", "admin-pass", Role.ADMIN);
        verify(userService, never()).register("user", "user-pass", Role.USER);
    }

    @Test
    void run_shouldSkipAdminSeed_whenAdminUsernameBlank() {
        properties.getAdmin().setUsername("");
        properties.getAdmin().setPassword("admin-pass");
        properties.getUser().setUsername("user");
        properties.getUser().setPassword("user-pass");

        when(userService.getUser("user")).thenReturn(null);

        seeder.run();

        verify(userService, never()).register("", "admin-pass", Role.ADMIN);
        verify(userService).register("user", "user-pass", Role.USER);
    }
}
