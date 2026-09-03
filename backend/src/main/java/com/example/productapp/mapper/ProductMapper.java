package com.example.productapp.mapper;

import com.example.productapp.dto.ProductRequestDto;
import com.example.productapp.dto.ProductResponseDto;
import com.example.productapp.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDto dto) {
        return Product.builder()
                .name(dto.name())
                .description(dto.description())
                .price(dto.price())
                .quantity(dto.quantity())
                .build();
    }

    public void updateEntity(Product entity, ProductRequestDto dto) {
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setPrice(dto.price());
        entity.setQuantity(dto.quantity());
    }

    public ProductResponseDto toResponseDto(Product entity) {
        return new ProductResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getQuantity(),
                entity.getCreatedDate(),
                entity.getUpdatedDate()
        );
    }
}
