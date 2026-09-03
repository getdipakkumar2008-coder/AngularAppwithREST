package com.example.productapp.controller;

import com.example.productapp.dto.ProductRequestDto;
import com.example.productapp.dto.ProductResponseDto;
import com.example.productapp.exception.ProductNotFoundException;
import com.example.productapp.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer unit tests for ProductController using MockMvc; service layer mocked.
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponseDto sampleResponse() {
        return new ProductResponseDto(1L, "Sample Product", "A sample",
                new BigDecimal("19.99"), 5, Instant.now(), Instant.now());
    }

    @Test
    void getAllProducts_returns200AndList() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Sample Product"));
    }

    @Test
    void getAllProducts_emptyList_returns200AndEmptyArray() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getProductById_existing_returns200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getProductById_missing_returns404() throws Exception {
        when(productService.getProductById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createProduct_validPayload_returns201() throws Exception {
        ProductRequestDto request = new ProductRequestDto("Sample Product", "A sample", new BigDecimal("19.99"), 5);
        when(productService.createProduct(any(ProductRequestDto.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createProduct_blankName_returns400() throws Exception {
        ProductRequestDto request = new ProductRequestDto("", "desc", new BigDecimal("19.99"), 5);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_negativePrice_returns400() throws Exception {
        ProductRequestDto request = new ProductRequestDto("Name", "desc", new BigDecimal("-1.00"), 5);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProduct_existing_returns200() throws Exception {
        ProductRequestDto request = new ProductRequestDto("Updated", "desc", new BigDecimal("29.99"), 10);
        when(productService.updateProduct(eq(1L), any(ProductRequestDto.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateProduct_missing_returns404() throws Exception {
        ProductRequestDto request = new ProductRequestDto("Updated", "desc", new BigDecimal("29.99"), 10);
        when(productService.updateProduct(eq(99L), any(ProductRequestDto.class)))
                .thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(put("/api/products/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_existing_returns204() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_missing_returns404() throws Exception {
        org.mockito.Mockito.doThrow(new ProductNotFoundException(99L))
                .when(productService).deleteProduct(99L);

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound());
    }
}
