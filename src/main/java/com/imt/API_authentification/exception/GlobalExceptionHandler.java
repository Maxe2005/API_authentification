package com.imt.API_authentification.exception;

import jakarta.xml.bind.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Errors> handleValidationException(Exception ex) {
        Errors errors = new Errors(new ArrayList<>());
        CustomError customError = new CustomError(400, ex.getMessage());
        errors.addError(customError);

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<Errors> handleNotFoundException(Exception ex) {
        Errors errors = new Errors(new ArrayList<>());
        CustomError customError = new CustomError(404, ex.getMessage());
        errors.addError(customError);

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<Errors> handleTokenInvalidException(Exception ex) {
        Errors errors = new Errors(new ArrayList<>());
        CustomError customError = new CustomError(498, ex.getMessage());
        errors.addError(customError);

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(UserDuplicateException.class)
    public ResponseEntity<Errors> handleUserDuplicateException(Exception ex) {
        Errors errors = new Errors(new ArrayList<>());
        CustomError customError = new CustomError(409, ex.getMessage());
        errors.addError(customError);

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(UserCredsException.class)
    public ResponseEntity<Errors> handleUserCredsException(Exception ex) {
        Errors errors = new Errors(new ArrayList<>());
        CustomError customError = new CustomError(401, ex.getMessage());
        errors.addError(customError);

        return ResponseEntity.badRequest().body(errors);
    }
}
