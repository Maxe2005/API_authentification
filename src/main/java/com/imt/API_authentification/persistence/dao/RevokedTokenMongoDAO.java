package com.imt.API_authentification.persistence.dao;

import com.imt.API_authentification.persistence.dto.RevokedTokenMongoDTO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RevokedTokenMongoDAO extends MongoRepository<RevokedTokenMongoDTO, String> {
}
