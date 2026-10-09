package com.cosmocats.marketplace.controller;

import com.cosmocats.marketplace.dto.ProductDto;
import com.cosmocats.marketplace.dto.ProductPageDto;
import com.cosmocats.marketplace.dto.ProductRequest;
import com.cosmocats.marketplace.mapper.ProductMapper;
import com.cosmocats.marketplace.service.ProductService;
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
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    private final ProductMapper mapper;

    public ProductController(ProductService service, ProductMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ProductDto get(@PathVariable @Min(1) long id) {
        return mapper.toDto(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductRequest request) {
        ProductDto created = mapper.toDto(service.create(mapper.toDomain(request)));
        return ResponseEntity.created(URI.create("/api/products/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ProductDto update(@PathVariable @Min(1) long id,
                             @Valid @RequestBody ProductRequest request) {
        return mapper.toDto(service.update(id, mapper.toDomain(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ProductPageDto list(@RequestParam(defaultValue = "0") @Min(0) int page,
                               @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return new ProductPageDto(
                service.findPage(page, size).stream().map(mapper::toDto).toList(),
                page,
                size,
                service.count()
        );
    }
}
