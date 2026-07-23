package com.imt.API_authentification.persistence.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Document(collection = "refresh_tokens")
@Getter
public class RefreshTokenMongoDTO {

    @MongoId
    private final String tokenHash;

    private final String username;

    @Indexed(expireAfterSeconds = 0)
    private final Instant expiresAt;

    @Setter
    private boolean revoked;

    public RefreshTokenMongoDTO(String tokenHash, String username, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.username = username;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }
}
