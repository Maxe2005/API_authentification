package com.imt.API_authentification.controller;

import com.imt.API_authentification.controller.dto.input.UserHttpDTO;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import com.imt.API_authentification.service.UserService;
import lombok.RequiredArgsConstructor;
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
    public String login(){
        return "login";
    }

}
