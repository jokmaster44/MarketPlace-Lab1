package com.cosmocats.marketplace.domain;


import java.util.List;

public record Cart(Long id, List<Product> products) {
}
