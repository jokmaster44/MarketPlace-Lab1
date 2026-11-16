package com.cosmocats.marketplace.controller;

import com.cosmocats.marketplace.dto.ProductDto;
import com.cosmocats.marketplace.dto.ProductPageDto;
import com.cosmocats.marketplace.dto.ProductRequest;
import com.cosmocats.marketplace.mapper.ProductMapper;
import com.cosmocats.marketplace.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/products", "/api/products"})
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping("/{id}")
    public ProductDto getProduct(@PathVariable @Min(1) long id) {
        return productMapper.toDto(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductRequest request,
                                                    HttpServletRequest httpRequest) {
        ProductDto createdProduct = productMapper.toDto(productService.create(productMapper.toDomain(request)));
        return ResponseEntity.created(URI.create(httpRequest.getRequestURI() + "/" + createdProduct.id()))
                .body(createdProduct);
    }

    @PutMapping("/{id}")
    public ProductDto updateProduct(@PathVariable @Min(1) long id,
                             @Valid @RequestBody ProductRequest request) {
        return productMapper.toDto(productService.update(id, productMapper.toDomain(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable @Min(1) long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ProductPageDto listProducts(@RequestParam(defaultValue = "0") @Min(0) int page,
                               @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return new ProductPageDto(
                productService.findPage(page, size).stream().map(productMapper::toDto).toList(),
                page,
                size,
                productService.count()
        );
    }
}
