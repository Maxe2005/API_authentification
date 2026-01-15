package com.imt.Api_authentification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imt.Api_authentification.controller.dto.input.ProductHttpDto;
import com.imt.Api_authentification.controller.dto.output.ProductDto;
import com.imt.Api_authentification.service.ProductService;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable @Valid @UUID(message= "bad uuid format   ") String productId) {
        ProductDto product = new ProductDto(productId.toString(), "Sample Product", 19.99);

        return ResponseEntity.ok(product);
    }

    @PostMapping("/create")
    public ResponseEntity<String> saveProduct(@RequestBody ProductHttpDto productHttpDto) {
        String savedProductId = productService.saveProduct(
                productHttpDto.getName(),
                productHttpDto.getPrice()
        );

        return ResponseEntity.ok(savedProductId);
    }

}
