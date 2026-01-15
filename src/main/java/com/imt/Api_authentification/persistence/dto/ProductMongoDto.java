package com.imt.Api_authentification.persistence.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.UUID;

@Getter
@Document(collection = "product")
@RequiredArgsConstructor
public class ProductMongoDto {

    @MongoId
    private final UUID id;
    private final String name;
    private final double price;

}
