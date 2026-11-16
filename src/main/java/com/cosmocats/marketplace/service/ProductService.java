package com.cosmocats.marketplace.service;

import com.cosmocats.marketplace.domain.Product;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductStore productStore = new InMemoryProductStore();

    /** Each call creates a new ID. Products with the same name are allowed. */
    public Product create(Product product) {
        return productStore.addProduct(product);
    }

    public Product findById(long id) {
        Product product = productStore.findProduct(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    public List<Product> findPage(int page, int size) {
        return productStore.findProductsPage(page, size);
    }

    public long count() {
        return productStore.productCount();
    }

    public Product update(long id, Product product) {
        Product updated = productStore.replaceProduct(id, product);
        if (updated == null) {
            throw new ProductNotFoundException(id);
        }
        return updated;
    }

    public void delete(long id) {
        if (!productStore.removeProduct(id)) {
            throw new ProductNotFoundException(id);
        }
    }
}
