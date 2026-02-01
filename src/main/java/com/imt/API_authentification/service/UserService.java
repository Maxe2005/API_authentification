package com.imt.API_authentification.service;

import com.imt.API_authentification.persistence.dao.UserMongoDAO;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMongoDAO userMongoDAO;

    public UserMongoDTO getUser(String username) {
        return userMongoDAO.findByUsername(username);
    }

    public boolean register(String username, String password) {

        if (getUser(username) != null) return false;

        UserMongoDTO user = new UserMongoDTO(
            UUID.randomUUID(),
            username,
            password
        );
        userMongoDAO.save(user);
        return true;
    }

    public void delete(String username) {
        UserMongoDTO user = getUser(username);
        userMongoDAO.delete(user);
    }
}
