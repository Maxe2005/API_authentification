package com.imt.API_authentification.persistence.dao;

import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface UserMongoDAO extends MongoRepository<UserMongoDTO, UUID> {
    UserMongoDTO findByUsername(String username);
}
