package com.cosmocats.marketplace.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @DecimalMin("0.0") @Digits(integer = 17, fraction = 2) BigDecimal price,
        @Min(1) Long categoryId
) {
    public ProductRequest(String name, BigDecimal price) {
        this(name, price, null);
    }
}
