package com.imt.Api_authentification.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Errors> handleValidationException(Exception ex) {
        Errors errors = new Errors(new ArrayList<>());
        CustomError customError = new CustomError(
                400,
                ex.getMessage()
        );
        errors.addError(customError);

        return ResponseEntity.badRequest().body(errors);
    }

}
