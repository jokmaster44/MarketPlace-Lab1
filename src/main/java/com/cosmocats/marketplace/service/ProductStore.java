package com.cosmocats.marketplace.service;

import com.cosmocats.marketplace.domain.Product;
import java.util.List;

interface ProductStore {
    Product addProduct(Product product);

    Product findProduct(long id);

    List<Product> findProductsPage(int page, int size);

    long productCount();

    Product replaceProduct(long id, Product product);

    boolean removeProduct(long id);
}
