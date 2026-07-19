package com.imt.API_authentification.config;

import com.imt.API_authentification.persistence.dto.Role;
import com.imt.API_authentification.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultUsersSeeder implements CommandLineRunner {

    private final UserService userService;
    private final DefaultUsersProperties properties;

    @Override
    public void run(String... args) {
        seed(properties.getAdmin(), Role.ADMIN);
        seed(properties.getUser(), Role.USER);
    }

    private void seed(DefaultUsersProperties.Account account, Role role) {
        if (account.getUsername() == null || account.getUsername().isBlank()
                || account.getPassword() == null || account.getPassword().isBlank()) {
            log.warn("Skipping default {} seed: username/password not configured", role);
            return;
        }
        if (userService.getUser(account.getUsername()) != null) {
            log.info("Default {} account '{}' already exists, skipping", role, account.getUsername());
            return;
        }
        userService.register(account.getUsername(), account.getPassword(), role);
        log.info("Seeded default {} account '{}'", role, account.getUsername());
    }
}
