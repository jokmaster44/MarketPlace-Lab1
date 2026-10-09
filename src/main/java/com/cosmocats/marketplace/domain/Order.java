package com.cosmocats.marketplace.domain;


import java.math.BigDecimal;
import java.util.List;

public record Order(Long id, List<Product> products, BigDecimal total) {
}
