package com.cosmocats.marketplace.service;

import com.cosmocats.marketplace.domain.Product;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ConcurrentHashMap<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public Product create(Product product) {
        long id = nextId.getAndIncrement();
        Product created = new Product(id, product.name(), product.price());
        products.put(id, created);
        return created;
    }

    public Product findById(long id) {
        Product product = products.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    public List<Product> findPage(int page, int size) {
        long offset = (long) page * size;
        return products.values().stream()
                .sorted(Comparator.comparing(Product::id))
                .skip(offset)
                .limit(size)
                .toList();
    }

    public long count() {
        return products.size();
    }

    public Product update(long id, Product product) {
        Product updated = new Product(id, product.name(), product.price());
        if (products.computeIfPresent(id, (key, oldProduct) -> updated) == null) {
            throw new ProductNotFoundException(id);
        }
        return updated;
    }

    public void delete(long id) {
        if (products.remove(id) == null) {
            throw new ProductNotFoundException(id);
        }
    }
}
