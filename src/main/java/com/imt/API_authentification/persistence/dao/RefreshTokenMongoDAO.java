package com.imt.API_authentification.persistence.dao;

import com.imt.API_authentification.persistence.dto.RefreshTokenMongoDTO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefreshTokenMongoDAO extends MongoRepository<RefreshTokenMongoDTO, String> {
    void deleteByUsername(String username);
}
