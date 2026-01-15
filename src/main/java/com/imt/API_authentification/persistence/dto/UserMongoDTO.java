package com.imt.API_authentification.persistence.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.UUID;

@Document(collection = "users")
@RequiredArgsConstructor
@Getter
@Setter
public class UserMongoDTO {

    @MongoId
    private final UUID id;
    private final String username;
    private final String password;

}
