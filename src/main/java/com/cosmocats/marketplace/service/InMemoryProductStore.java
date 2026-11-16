package com.cosmocats.marketplace.service;

import com.cosmocats.marketplace.domain.Product;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

final class InMemoryProductStore implements ProductStore {
    private final ConcurrentHashMap<Long, Product> productsById = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Product addProduct(Product product) {
        long id = nextId.getAndIncrement();
        Product createdProduct = new Product(id, product.name(), product.price());
        productsById.put(id, createdProduct);
        return createdProduct;
    }

    @Override
    public Product findProduct(long id) {
        return productsById.get(id);
    }

    @Override
    public List<Product> findProductsPage(int page, int size) {
        long offset = (long) page * size;
        return productsById.values().stream()
                .sorted(Comparator.comparing(Product::id))
                .skip(offset)
                .limit(size)
                .toList();
    }

    @Override
    public long productCount() {
        return productsById.size();
    }

    @Override
    public Product replaceProduct(long id, Product product) {
        Product replacement = new Product(id, product.name(), product.price());
        return productsById.computeIfPresent(id, (key, oldProduct) -> replacement);
    }

    @Override
    public boolean removeProduct(long id) {
        return productsById.remove(id) != null;
    }
}
