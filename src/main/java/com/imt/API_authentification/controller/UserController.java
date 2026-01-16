package com.imt.API_authentification.controller;

import com.imt.API_authentification.controller.dto.input.UserHttpDTO;
import com.imt.API_authentification.controller.dto.output.LoginHttpDTO;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import com.imt.API_authentification.service.UserService;
import com.imt.API_authentification.utils.AuthHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<UserMongoDTO> getUser(@PathVariable String username) {
        UserMongoDTO user = userService.getUser(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<UserHttpDTO> register(@RequestBody UserHttpDTO userHttpDTO){
        if (userService.register(userHttpDTO.getUsername(), userHttpDTO.getPassword())) {
            return ResponseEntity.ok(userHttpDTO);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginHttpDTO> login(@RequestBody UserHttpDTO userHttpDTO){
        UserMongoDTO user = userService.getUser(userHttpDTO.getUsername());
        if (user != null) {
            if (user.getPassword().equals(userHttpDTO.getPassword())) {
                return ResponseEntity.ok(new LoginHttpDTO("token"));
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/verify-token/{token}")
    public HttpStatus verifyToken(@RequestBody String token) {
        if (AuthHandler.validateToken(token)) {
            return HttpStatus.OK;
        } else {
            return HttpStatus.UNAUTHORIZED;
        }
    }
}
