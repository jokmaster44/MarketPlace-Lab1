package com.cosmocats.marketplace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cosmocats.marketplace.domain.Product;
import com.cosmocats.marketplace.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.persistence.entity.ProductEntity;
import com.cosmocats.marketplace.persistence.repository.CategoryRepository;
import com.cosmocats.marketplace.persistence.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

class ProductServiceTest {
    private ProductRepository products;
    private CategoryRepository categories;
    private ProductService service;

    @BeforeEach
    void setUp() {
        products = mock(ProductRepository.class);
        categories = mock(CategoryRepository.class);
        service = new ProductService(products, categories);
    }

    // Test case: Creating a product saves it through the repository.
    @Test
    void test_create_returnsSavedProduct_whenInputIsValid() {
        when(products.save(any(ProductEntity.class))).thenAnswer(invocation -> {
            ProductEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 1L);
            return entity;
        });

        Product created = service.create(new Product(null, "Cosmic food", new BigDecimal("19.99")));

        assertEquals(1L, created.id(), "The repository-assigned ID should be returned");
        assertEquals("Cosmic food", created.name(), "The name should be stored");
        assertEquals(new BigDecimal("19.99"), created.price(), "The price should be stored");
    }

    // Test case: An existing product is loaded from the repository.
    @Test
    void test_findById_returnsProduct_whenIdExists() {
        when(products.findById(1L)).thenReturn(Optional.of(entity(1L, "Cosmic food", "19.99")));

        Product found = service.findById(1L);

        assertEquals(1L, found.id(), "The requested product ID should be returned");
        assertEquals("Cosmic food", found.name(), "The requested product name should be returned");
    }

    // Test case: Looking up an unknown product reports its ID.
    @Test
    void test_findById_throwsNotFound_whenProductDoesNotExist() {
        when(products.findById(99L)).thenReturn(Optional.empty());

        ProductNotFoundException error = assertThrows(ProductNotFoundException.class,
                () -> service.findById(99L), "An unknown ID should fail");

        assertEquals("Product with id 99 was not found", error.getMessage(),
                "The error should identify the missing product");
    }

    // Test case: The requested page is returned in ID order.
    @Test
    void test_findPage_returnsRequestedProducts_whenPageExists() {
        when(products.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(
                List.of(entity(1L, "First", "1.00"), entity(2L, "Second", "2.00"))));

        List<Product> page = service.findPage(0, 2);

        assertEquals(List.of(1L, 2L), page.stream().map(Product::id).toList(),
                "The page should contain the products returned by the repository");
    }

    // Test case: The product count is read from the repository.
    @Test
    void test_count_returnsRepositoryCount_whenProductsExist() {
        when(products.count()).thenReturn(3L);

        assertEquals(3L, service.count(), "The service should return the database product count");
    }

    // Test case: Updating a product preserves its ID and replaces its fields.
    @Test
    void test_update_replacesFields_whenIdExists() {
        ProductEntity stored = entity(1L, "Old", "1.00");
        when(products.findById(1L)).thenReturn(Optional.of(stored));
        when(products.save(stored)).thenReturn(stored);

        Product updated = service.update(1L, new Product(null, "New", new BigDecimal("2.50")));

        assertEquals(1L, updated.id(), "Updating should keep the existing ID");
        assertEquals("New", updated.name(), "Updating should replace the name");
        assertEquals(new BigDecimal("2.50"), updated.price(), "Updating should replace the price");
        verify(products).save(stored);
    }

    // Test case: Updating an unknown product reports its ID.
    @Test
    void test_update_throwsNotFound_whenProductDoesNotExist() {
        when(products.findById(99L)).thenReturn(Optional.empty());

        ProductNotFoundException error = assertThrows(ProductNotFoundException.class,
                () -> service.update(99L, new Product(null, "New", BigDecimal.ONE)),
                "Updating an unknown ID should fail");

        assertTrue(error.getMessage().contains("99"), "The error should identify the missing ID");
    }

    // Test case: Deleting an existing product calls the repository.
    @Test
    void test_delete_removesProduct_whenIdExists() {
        ProductEntity stored = entity(1L, "Cosmic food", "19.99");
        when(products.findById(1L)).thenReturn(Optional.of(stored));

        service.delete(1L);

        verify(products).delete(stored);
    }

    // Test case: Deleting an unknown product reports its ID.
    @Test
    void test_delete_throwsNotFound_whenProductDoesNotExist() {
        when(products.findById(99L)).thenReturn(Optional.empty());

        ProductNotFoundException error = assertThrows(ProductNotFoundException.class,
                () -> service.delete(99L), "Deleting an unknown ID should fail");

        assertTrue(error.getMessage().contains("99"), "The error should identify the missing ID");
    }

    // Test case: A product can be created in an existing category.
    @Test
    void test_create_attachesCategory_whenCategoryExists() {
        CategoryEntity category = new CategoryEntity("Food");
        ReflectionTestUtils.setField(category, "id", 4L);
        when(categories.findById(4L)).thenReturn(Optional.of(category));
        when(products.save(any(ProductEntity.class))).thenAnswer(invocation -> {
            ProductEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 1L);
            return entity;
        });

        Product created = service.create(new Product(null, "Cosmic food", BigDecimal.ONE, 4L));

        assertEquals(4L, created.categoryId(), "The saved product should keep its category ID");
    }

    // Test case: An unknown category is rejected before saving a product.
    @Test
    void test_create_throwsNotFound_whenCategoryDoesNotExist() {
        when(categories.findById(99L)).thenReturn(Optional.empty());

        CategoryNotFoundException error = assertThrows(CategoryNotFoundException.class,
                () -> service.create(new Product(null, "Cosmic food", BigDecimal.ONE, 99L)),
                "A missing category should be rejected");

        assertTrue(error.getMessage().contains("99"), "The error should identify the missing category");
    }

    private static ProductEntity entity(Long id, String name, String price) {
        ProductEntity entity = new ProductEntity(name, new BigDecimal(price), null);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
