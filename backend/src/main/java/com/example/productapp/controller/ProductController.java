package com.example.productapp.controller;

import com.example.productapp.dto.ProductRequestDto;
import com.example.productapp.dto.ProductResponseDto;
import com.example.productapp.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto request) {
        ProductResponseDto created = productService.createProduct(request);
        return ResponseEntity
                .created(URI.create("/api/products/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable Long id,
                                                              @Valid @RequestBody ProductRequestDto request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
