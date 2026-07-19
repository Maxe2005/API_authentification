package com.imt.API_authentification.service;

import com.imt.API_authentification.persistence.dao.UserMongoDAO;
import com.imt.API_authentification.persistence.dto.Role;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMongoDAO userMongoDAO;
    private final PasswordEncoder passwordEncoder;

    public UserMongoDTO getUser(String username) {
        return userMongoDAO.findByUsername(username);
    }

    public boolean register(String username, String password, Role role) {

        if (getUser(username) != null) return false;

        UserMongoDTO user = new UserMongoDTO(
            UUID.randomUUID(),
            username,
            passwordEncoder.encode(password),
            role
        );
        userMongoDAO.save(user);
        return true;
    }

    public boolean checkPassword(UserMongoDTO user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void delete(String username) {
        UserMongoDTO user = getUser(username);
        if (user == null) return;
        userMongoDAO.delete(user);
    }
}
