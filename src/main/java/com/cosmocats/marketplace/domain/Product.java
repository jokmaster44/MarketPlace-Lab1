package com.cosmocats.marketplace.domain;

import java.math.BigDecimal;

public record Product(Long id, String name, BigDecimal price, Long categoryId) {
    public Product(Long id, String name, BigDecimal price) {
        this(id, name, price, null);
    }
}
