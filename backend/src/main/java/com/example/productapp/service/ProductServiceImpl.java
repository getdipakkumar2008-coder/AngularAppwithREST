package com.example.productapp.service;

import com.example.productapp.dto.ProductRequestDto;
import com.example.productapp.dto.ProductResponseDto;
import com.example.productapp.entity.Product;
import com.example.productapp.exception.ProductNotFoundException;
import com.example.productapp.mapper.ProductMapper;
import com.example.productapp.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toResponseDto(product);
    }

    @Override
    public ProductResponseDto createProduct(ProductRequestDto request) {
        Product entity = productMapper.toEntity(request);
        Product saved = productRepository.save(entity);
        return productMapper.toResponseDto(saved);
    }

    @Override
    public ProductResponseDto updateProduct(Long id, ProductRequestDto request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productMapper.updateEntity(existing, request);
        Product saved = productRepository.save(existing);
        return productMapper.toResponseDto(saved);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }
}
