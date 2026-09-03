package com.example.productapp.service;

import com.example.productapp.dto.ProductRequestDto;
import com.example.productapp.dto.ProductResponseDto;

import java.util.List;

public interface ProductService {
    List<ProductResponseDto> getAllProducts();

    ProductResponseDto getProductById(Long id);

    ProductResponseDto createProduct(ProductRequestDto request);

    ProductResponseDto updateProduct(Long id, ProductRequestDto request);

    void deleteProduct(Long id);
}
