package com.cosmocats.marketplace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmocats.marketplace.domain.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductServiceTest {

    // Test case: A valid product gets an ID and is stored.
    @Test
    void test_create_assignsIdAndStoresProduct_whenInputIsValid() {
        ProductService service = new ProductService();
        Product input = new Product(null, "Cosmic cat food", new BigDecimal("19.99"));

        Product created = service.create(input);

        assertEquals(1L, created.id(), "The first created product should receive ID 1");
        assertEquals("Cosmic cat food", created.name(), "The product name should be preserved");
        assertEquals(new BigDecimal("19.99"), created.price(), "The product price should be preserved");
        assertEquals(created, service.findById(created.id()), "The created product should be stored");
        assertEquals(1L, service.count(), "The service should contain one product");
    }

    // Test case: Looking up a stored product returns that product.
    @Test
    void test_findById_returnsProduct_whenIdExists() {
        ProductService service = new ProductService();
        Product created = service.create(new Product(null, "Cosmic milk", new BigDecimal("5.50")));

        Product found = service.findById(created.id());

        assertEquals(created, found, "Looking up an existing ID should return the stored product");
    }

    // Test case: Looking up an unknown product reports the missing ID.
    @Test
    void test_findById_throwsNotFound_whenProductDoesNotExist() {
        ProductService service = new ProductService();

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class, () -> service.findById(99L),
                "Looking up an unknown ID should fail");

        assertEquals("Product with id 99 was not found", exception.getMessage(),
                "The error should identify the missing product");
    }

    // Test case: Pages contain products ordered by ID and use zero-based page numbers.
    @Test
    void test_findPage_returnsRequestedSlice_whenProductsExist() {
        ProductService service = new ProductService();
        Product first = service.create(new Product(null, "Moon dust", new BigDecimal("1.00")));
        Product second = service.create(new Product(null, "Star map", new BigDecimal("2.00")));
        Product third = service.create(new Product(null, "Cosmic milk", new BigDecimal("3.00")));

        List<Product> firstPage = service.findPage(0, 2);
        List<Product> secondPage = service.findPage(1, 2);
        List<Product> beyondLastPage = service.findPage(2, 2);

        assertEquals(List.of(first, second), firstPage,
                "Page 0 should contain the first two products in ID order");
        assertEquals(List.of(third), secondPage,
                "Page 1 should contain the remaining product");
        assertTrue(beyondLastPage.isEmpty(), "A page beyond the end should be empty");
    }

    // Test case: A new service has no products.
    @Test
    void test_count_returnsZero_whenNoProductsExist() {
        ProductService service = new ProductService();

        assertEquals(0L, service.count(), "A new service should contain no products");
    }

    // Test case: Updating a stored product replaces its data without changing its ID.
    @Test
    void test_update_replacesProduct_whenIdExists() {
        ProductService service = new ProductService();
        Product created = service.create(new Product(null, "Old name", new BigDecimal("1.00")));
        Product replacement = new Product(null, "New name", new BigDecimal("2.50"));

        Product updated = service.update(created.id(), replacement);

        assertEquals(created.id(), updated.id(), "Updating should keep the original ID");
        assertEquals("New name", updated.name(), "Updating should replace the name");
        assertEquals(new BigDecimal("2.50"), updated.price(), "Updating should replace the price");
        assertEquals(updated, service.findById(created.id()), "The replacement should be stored");
        assertEquals(1L, service.count(), "Updating should not add another product");
    }

    // Test case: Updating an unknown product reports the missing ID.
    @Test
    void test_update_throwsNotFound_whenProductDoesNotExist() {
        ProductService service = new ProductService();
        Product input = new Product(null, "Cosmic cat food", new BigDecimal("19.99"));

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class, () -> service.update(99L, input),
                "Updating an unknown ID should fail");

        assertEquals("Product with id 99 was not found", exception.getMessage(),
                "The error should identify the missing product");
    }

    // Test case: Deleting a stored product removes it.
    @Test
    void test_delete_removesProduct_whenIdExists() {
        ProductService service = new ProductService();
        Product created = service.create(new Product(null, "Star map", new BigDecimal("2.00")));

        service.delete(created.id());

        assertEquals(0L, service.count(), "Deleting should reduce the product count");
        assertThrows(ProductNotFoundException.class, () -> service.findById(created.id()),
                "The deleted product should no longer be retrievable");
    }

    // Test case: Deleting an unknown product reports the missing ID.
    @Test
    void test_delete_throwsNotFound_whenProductDoesNotExist() {
        ProductService service = new ProductService();

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class, () -> service.delete(99L),
                "Deleting an unknown ID should fail");

        assertEquals("Product with id 99 was not found", exception.getMessage(),
                "The error should identify the missing product");
    }
}
