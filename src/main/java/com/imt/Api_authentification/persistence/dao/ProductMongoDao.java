package com.imt.Api_authentification.persistence.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.imt.Api_authentification.persistence.dto.ProductMongoDto;

import java.util.UUID;

public interface ProductMongoDao extends MongoRepository<ProductMongoDto, UUID> {

}
