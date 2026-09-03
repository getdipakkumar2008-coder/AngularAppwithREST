package com.example.productapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequestDto(
        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must be at most 255 characters")
        String name,

        @Size(max = 1000, message = "must be at most 1000 characters")
        String description,

        @NotNull(message = "must not be null")
        @PositiveOrZero(message = "must be greater than or equal to 0")
        BigDecimal price,

        @NotNull(message = "must not be null")
        @PositiveOrZero(message = "must be greater than or equal to 0")
        Integer quantity
) {
}
