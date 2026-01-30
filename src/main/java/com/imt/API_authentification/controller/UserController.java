package com.imt.API_authentification.controller;

import com.imt.API_authentification.controller.dto.input.TokenHttpRequestDTO;
import com.imt.API_authentification.controller.dto.input.UserHttpDTO;
import com.imt.API_authentification.controller.dto.output.LoginHttpDTO;
import com.imt.API_authentification.controller.dto.output.TokenHttpResponseDTO;
import com.imt.API_authentification.exception.TokenExpiredException;
import com.imt.API_authentification.exception.UserCredsException;
import com.imt.API_authentification.exception.UserDuplicateException;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import com.imt.API_authentification.service.UserService;
import com.imt.API_authentification.utils.AuthHandler;
import jakarta.xml.bind.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthHandler authHandler;

    @PostMapping
    public ResponseEntity<LoginHttpDTO> register(@RequestBody UserHttpDTO userHttpDTO) throws ValidationException, UserDuplicateException {
        if (userHttpDTO.getUsername() == null) throw new ValidationException("Empty username");
        if (userHttpDTO.getPassword() == null) throw new ValidationException("Empty password");

        if (userService.register(userHttpDTO.getUsername(), userHttpDTO.getPassword())) {
            return ResponseEntity.ok(new LoginHttpDTO(authHandler.generateToken(userHttpDTO.getUsername())));
        } else throw new UserDuplicateException("User already exists");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginHttpDTO> login(@RequestBody UserHttpDTO userHttpDTO) throws ValidationException, UserCredsException {
        if (userHttpDTO.getUsername() == null) throw new ValidationException("Empty username");
        if (userHttpDTO.getPassword() == null) throw new ValidationException("Empty password");

        UserMongoDTO user = userService.getUser(userHttpDTO.getUsername());
        if (user == null) throw new ValidationException("User not found");

        if (user.getPassword().equals(userHttpDTO.getPassword())) {
            return ResponseEntity.ok(new LoginHttpDTO(authHandler.generateToken(user.getUsername())));
        } else throw new UserCredsException("Incorrect username or password");
    }

    @PostMapping("/verify-token")
    public ResponseEntity<TokenHttpResponseDTO> verifyToken(@RequestBody TokenHttpRequestDTO tokenHttpRequestDTO) throws TokenExpiredException {
        String user = authHandler.validateToken(tokenHttpRequestDTO.getToken());
        if (user != null) {
            return ResponseEntity.ok(new TokenHttpResponseDTO(user));
        } else throw new TokenExpiredException("Token expired");
    }

    @PostMapping("/delete")
    public HttpStatus delete(@RequestBody TokenHttpRequestDTO tokenHttpRequestDTO) {
        String user = authHandler.validateToken(tokenHttpRequestDTO.getToken());
        if (user != null) {
            userService.delete(user);
            return HttpStatus.OK;
        }
        return HttpStatus.UNAUTHORIZED;
    }
}
