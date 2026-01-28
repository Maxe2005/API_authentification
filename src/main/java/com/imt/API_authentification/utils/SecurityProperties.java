package com.imt.API_authentification.utils;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private String secret;
    private String salt;
}
