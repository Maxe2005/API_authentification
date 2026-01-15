package com.imt.Api_authentification.service;

import org.springframework.stereotype.Service;

import com.imt.Api_authentification.service.port.ProductPort;

@Service
public class ProductService {

    private final ProductPort productPort;

    public ProductService(ProductPort productPort) {
        this.productPort = productPort;
    }

    public String saveProduct(String name, double price) {
        return productPort.save(name, price);
    }

}
