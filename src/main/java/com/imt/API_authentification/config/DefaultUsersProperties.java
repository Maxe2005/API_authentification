package com.imt.API_authentification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.default-users")
public class DefaultUsersProperties {

    private Account admin = new Account();
    private Account user = new Account();

    @Getter
    @Setter
    public static class Account {
        private String username;
        private String password;
    }
}
