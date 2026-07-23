package com.imt.API_authentification.controller;

import com.imt.API_authentification.controller.dto.input.AdminRegisterHttpDTO;
import com.imt.API_authentification.controller.dto.input.LogoutHttpRequestDTO;
import com.imt.API_authentification.controller.dto.input.RefreshTokenHttpRequestDTO;
import com.imt.API_authentification.controller.dto.input.TokenHttpRequestDTO;
import com.imt.API_authentification.controller.dto.input.UserHttpDTO;
import com.imt.API_authentification.controller.dto.output.LoginHttpDTO;
import com.imt.API_authentification.controller.dto.output.TokenHttpResponseDTO;
import com.imt.API_authentification.exception.InsufficientRoleException;
import com.imt.API_authentification.exception.TokenInvalidException;
import com.imt.API_authentification.exception.UserCredsException;
import com.imt.API_authentification.exception.UserDuplicateException;
import com.imt.API_authentification.persistence.dto.Role;
import com.imt.API_authentification.persistence.dto.UserMongoDTO;
import com.imt.API_authentification.service.AuthorizationService;
import com.imt.API_authentification.service.TokenPair;
import com.imt.API_authentification.service.UserService;
import com.imt.API_authentification.utils.AuthenticatedUser;
import com.imt.API_authentification.utils.UserValidator;
import jakarta.xml.bind.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthorizationService authorizationService;

    @PostMapping
    public ResponseEntity<LoginHttpDTO> register(@RequestBody UserHttpDTO userHttpDTO) throws ValidationException, UserDuplicateException {
        UserValidator.validateUsername(userHttpDTO.getUsername());
        UserValidator.validatePassword(userHttpDTO.getPassword());

        if (userService.register(userHttpDTO.getUsername(), userHttpDTO.getPassword(), Role.USER)) {
            TokenPair tokens = authorizationService.issueTokenPair(userHttpDTO.getUsername(), Role.USER);
            return ResponseEntity.ok(new LoginHttpDTO(tokens.accessToken(), tokens.refreshToken()));
        } else throw new UserDuplicateException("User already exists");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginHttpDTO> login(@RequestBody UserHttpDTO userHttpDTO) throws ValidationException, UserCredsException {
        if (userHttpDTO.getUsername() == null) throw new ValidationException("Empty username");
        if (userHttpDTO.getPassword() == null) throw new ValidationException("Empty password");

        UserMongoDTO user = userService.getUser(userHttpDTO.getUsername());
        if (user == null) throw new ValidationException("User not found");

        if (userService.checkPassword(user, userHttpDTO.getPassword())) {
            TokenPair tokens = authorizationService.issueTokenPair(user.getUsername(), user.getRole());
            return ResponseEntity.ok(new LoginHttpDTO(tokens.accessToken(), tokens.refreshToken()));
        } else throw new UserCredsException("Incorrect username or password");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginHttpDTO> refreshToken(@RequestBody RefreshTokenHttpRequestDTO refreshTokenHttpRequestDTO) throws TokenInvalidException {
        TokenPair tokens = authorizationService.refresh(refreshTokenHttpRequestDTO.getRefreshToken());
        return ResponseEntity.ok(new LoginHttpDTO(tokens.accessToken(), tokens.refreshToken()));
    }

    @PostMapping("/verify-token")
    public ResponseEntity<TokenHttpResponseDTO> verifyToken(@RequestBody TokenHttpRequestDTO tokenHttpRequestDTO) throws TokenInvalidException {
        AuthenticatedUser user = authorizationService.requireValidToken(tokenHttpRequestDTO.getToken());
        return ResponseEntity.ok(new TokenHttpResponseDTO(user.username(), user.role()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutHttpRequestDTO logoutHttpRequestDTO) throws TokenInvalidException {
        authorizationService.logout(logoutHttpRequestDTO.getToken(), logoutHttpRequestDTO.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody TokenHttpRequestDTO tokenHttpRequestDTO) throws TokenInvalidException {
        AuthenticatedUser caller = authorizationService.requireValidToken(tokenHttpRequestDTO.getToken());
        userService.delete(caller.username());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/register")
    public ResponseEntity<LoginHttpDTO> adminRegister(@RequestBody AdminRegisterHttpDTO adminRegisterHttpDTO)
            throws TokenInvalidException, InsufficientRoleException, ValidationException, UserDuplicateException {
        authorizationService.requireAdmin(adminRegisterHttpDTO.getToken());

        UserValidator.validateUsername(adminRegisterHttpDTO.getUsername());
        UserValidator.validatePassword(adminRegisterHttpDTO.getPassword());
        if (adminRegisterHttpDTO.getRole() == null) throw new ValidationException("Empty role");

        if (userService.register(adminRegisterHttpDTO.getUsername(), adminRegisterHttpDTO.getPassword(), adminRegisterHttpDTO.getRole())) {
            TokenPair tokens = authorizationService.issueTokenPair(adminRegisterHttpDTO.getUsername(), adminRegisterHttpDTO.getRole());
            return ResponseEntity.ok(new LoginHttpDTO(tokens.accessToken(), tokens.refreshToken()));
        } else throw new UserDuplicateException("User already exists");
    }

    @PostMapping("/admin/delete/{username}")
    public ResponseEntity<Void> adminDelete(@PathVariable String username, @RequestBody TokenHttpRequestDTO tokenHttpRequestDTO)
            throws TokenInvalidException, InsufficientRoleException, ValidationException {
        authorizationService.requireAdmin(tokenHttpRequestDTO.getToken());

        if (userService.getUser(username) == null) throw new ValidationException("User not found");

        userService.delete(username);
        return ResponseEntity.noContent().build();
    }
}
