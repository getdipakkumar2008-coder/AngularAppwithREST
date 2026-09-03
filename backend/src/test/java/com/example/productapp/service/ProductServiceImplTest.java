package com.example.productapp.service;

import com.example.productapp.dto.ProductRequestDto;
import com.example.productapp.dto.ProductResponseDto;
import com.example.productapp.entity.Product;
import com.example.productapp.exception.ProductNotFoundException;
import com.example.productapp.mapper.ProductMapper;
import com.example.productapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductServiceImpl.
 * Written before implementation exists (TDD) per Plan.md Phase 0/1.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequestDto requestDto;
    private ProductResponseDto responseDto;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Sample Product")
                .description("A sample")
                .price(new BigDecimal("19.99"))
                .quantity(5)
                .createdDate(Instant.now())
                .updatedDate(Instant.now())
                .build();

        requestDto = new ProductRequestDto("Sample Product", "A sample", new BigDecimal("19.99"), 5);
        responseDto = new ProductResponseDto(1L, "Sample Product", "A sample",
                new BigDecimal("19.99"), 5, product.getCreatedDate(), product.getUpdatedDate());
    }

    @Test
    void getAllProducts_returnsMappedList() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toResponseDto(product)).thenReturn(responseDto);

        List<ProductResponseDto> result = productService.getAllProducts();

        assertThat(result).hasSize(1).containsExactly(responseDto);
        verify(productRepository).findAll();
    }

    @Test
    void getAllProducts_emptyRepository_returnsEmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<ProductResponseDto> result = productService.getAllProducts();

        assertThat(result).isEmpty();
    }

    @Test
    void getProductById_existingId_returnsDto() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponseDto(product)).thenReturn(responseDto);

        ProductResponseDto result = productService.getProductById(1L);

        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    void getProductById_missingId_throwsNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void createProduct_savesAndReturnsDto() {
        when(productMapper.toEntity(requestDto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponseDto(product)).thenReturn(responseDto);

        ProductResponseDto result = productService.createProduct(requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_existingId_updatesFieldsAndSaves() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponseDto(product)).thenReturn(responseDto);

        ProductResponseDto result = productService.updateProduct(1L, requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_missingId_throwsNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, requestDto))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_existingId_deletesSuccessfully() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_missingId_throwsNotFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }
}
