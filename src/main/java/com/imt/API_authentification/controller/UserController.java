package com.imt.API_authentification.controller;

import com.imt.API_authentification.controller.dto.input.TokenHttpRequestDTO;
import com.imt.API_authentification.controller.dto.input.UserHttpDTO;
import com.imt.API_authentification.controller.dto.output.LoginHttpDTO;
import com.imt.API_authentification.controller.dto.output.TokenHttpResponseDTO;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import com.imt.API_authentification.service.UserService;
import com.imt.API_authentification.utils.AuthHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<LoginHttpDTO> register(@RequestBody UserHttpDTO userHttpDTO){
        if (userService.register(userHttpDTO.getUsername(), userHttpDTO.getPassword())) {
            return ResponseEntity.ok(new LoginHttpDTO(AuthHandler.generateToken(userHttpDTO.getUsername())));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginHttpDTO> login(@RequestBody UserHttpDTO userHttpDTO){
        UserMongoDTO user = userService.getUser(userHttpDTO.getUsername());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (user.getPassword().equals(userHttpDTO.getPassword())) {
            return ResponseEntity.ok(new LoginHttpDTO(AuthHandler.generateToken(user.getUsername())));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/verify-token")
    public ResponseEntity<TokenHttpResponseDTO> verifyToken(@RequestBody TokenHttpRequestDTO tokenHttpRequestDTO) {
        log.info("Received token: {}", tokenHttpRequestDTO.getToken());
        String user = AuthHandler.validateToken(tokenHttpRequestDTO.getToken());
        if (user != null) {
            return ResponseEntity.ok(new TokenHttpResponseDTO(user));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
