package com.imt.API_authentification.persistence.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Document(collection = "revoked_tokens")
@RequiredArgsConstructor
@Getter
public class RevokedTokenMongoDTO {

    @MongoId
    private final String tokenHash;

    @Indexed(expireAfterSeconds = 0)
    private final Instant expiresAt;
}
