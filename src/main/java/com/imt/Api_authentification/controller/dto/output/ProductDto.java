package com.imt.Api_authentification.controller.dto.output;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ProductDto {

    public ProductDto(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    private String id;
    private String name;
    private double price;

}
