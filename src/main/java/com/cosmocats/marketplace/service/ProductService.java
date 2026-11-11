package com.cosmocats.marketplace.service;

import com.cosmocats.marketplace.domain.Product;
import com.cosmocats.marketplace.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.persistence.entity.ProductEntity;
import com.cosmocats.marketplace.persistence.repository.CategoryRepository;
import com.cosmocats.marketplace.persistence.repository.ProductRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;

    public ProductService(ProductRepository products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    public Product create(Product product) {
        ProductEntity entity = new ProductEntity(product.name(), product.price(), category(product.categoryId()));
        return toDomain(products.save(entity));
    }

    @Transactional(readOnly = true)
    public Product findById(long id) {
        return toDomain(products.findById(id).orElseThrow(() -> new ProductNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public List<Product> findPage(int page, int size) {
        return products.findAll(PageRequest.of(page, size, Sort.by("id"))).getContent()
                .stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public long count() {
        return products.count();
    }

    public Product update(long id, Product product) {
        ProductEntity entity = products.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        entity.replace(product.name(), product.price(), category(product.categoryId()));
        return toDomain(products.save(entity));
    }

    public void delete(long id) {
        ProductEntity entity = products.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        products.delete(entity);
    }

    private CategoryEntity category(Long id) {
        if (id == null) {
            return null;
        }
        return categories.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }

    private Product toDomain(ProductEntity entity) {
        Long categoryId = entity.getCategory() == null ? null : entity.getCategory().getId();
        return new Product(entity.getId(), entity.getName(), entity.getPrice(), categoryId);
    }
}
