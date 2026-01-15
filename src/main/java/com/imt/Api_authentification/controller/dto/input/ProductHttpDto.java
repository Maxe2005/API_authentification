package com.imt.Api_authentification.controller.dto.input;

import lombok.Getter;

@Getter
public class ProductHttpDto {

    public ProductHttpDto(String name, double price) {
        this.name = name;
        this.price = price;
    }

    private String name;
    private double price;

}
