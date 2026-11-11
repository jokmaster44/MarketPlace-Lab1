package com.cosmocats.marketplace.dto;

import java.math.BigDecimal;

public record ProductDto(Long id, String name, BigDecimal price, Long categoryId) {
    public ProductDto(Long id, String name, BigDecimal price) {
        this(id, name, price, null);
    }
}
