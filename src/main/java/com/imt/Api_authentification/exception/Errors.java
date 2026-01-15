package com.imt.Api_authentification.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Errors {

    private List<CustomError> errors;

    public void addError(CustomError error) {
        this.errors.add(error);
    }

}
