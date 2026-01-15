//package com.imt.demo2026.controller;
//
//import com.imt.demo2026.controller.dto.input.ProductHttpDto;
//import com.imt.demo2026.controller.dto.output.ProductDto;
//import com.imt.demo2026.service.ProductService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.ResponseEntity;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class ProductControllerTest {
//
//    private ProductController productController;
//
//    @Mock
//    private ProductService productService;
//
//    @BeforeEach
//    void setUp() {
//        productController = new ProductController(productService);
//    }
//
//    @Test
//    void shouldReturnProductById() {
//        // Arrange
//        String productId = "123";
//
//        // Act
////        var response = productController.getProduct(productId);
//
//        // Assert
//        ProductDto expectedProduct = new ProductDto("123", "Sample Product", 19.99);
//
//        assertThat(response.getBody()).isEqualTo(expectedProduct);
//    }
//
//    @Test
//    void shouldSaveProduct() {
//        // Arrange
//        ProductHttpDto productHttpDto = new ProductHttpDto("Sample Product", 19.99);
//        when(productService.saveProduct("Sample Product", 19.99)).thenReturn("productId");
//
//        // Act
//        ResponseEntity<String> response = productController.saveProduct(productHttpDto);
//
//        // Assert
//        assertThat(response.getStatusCode().value()).isEqualTo(200);
//        assertThat(response.getBody()).isEqualTo("productId");
//    }
//
//}