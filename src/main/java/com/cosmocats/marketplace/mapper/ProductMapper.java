package com.cosmocats.marketplace.mapper;

import com.cosmocats.marketplace.domain.Product;
import com.cosmocats.marketplace.dto.ProductDto;
import com.cosmocats.marketplace.dto.ProductRequest;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductDto toDto(Product product) {
        return new ProductDto(product.id(), product.name(), product.price());
    }

    public Product toDomain(ProductRequest request) {
        return new Product(null, request.name(), request.price());
    }
}
