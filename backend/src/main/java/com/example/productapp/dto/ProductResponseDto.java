package com.example.productapp.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        Instant createdDate,
        Instant updatedDate
) {
}
