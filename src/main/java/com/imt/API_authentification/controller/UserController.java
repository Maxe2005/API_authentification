package com.imt.API_authentification.controller;

import com.imt.API_authentification.controller.dto.input.UserHttpDTO;
import com.imt.API_authentification.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
