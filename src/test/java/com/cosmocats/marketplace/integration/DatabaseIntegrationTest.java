package com.cosmocats.marketplace.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmocats.marketplace.domain.Category;
import com.cosmocats.marketplace.domain.Order;
import com.cosmocats.marketplace.domain.Product;
import com.cosmocats.marketplace.persistence.repository.OrderRepository;
import com.cosmocats.marketplace.persistence.repository.ProductRepository;
import com.cosmocats.marketplace.service.CategoryNotFoundException;
import com.cosmocats.marketplace.service.CategoryService;
import com.cosmocats.marketplace.service.OrderNotFoundException;
import com.cosmocats.marketplace.service.OrderService;
import com.cosmocats.marketplace.service.ProductNotFoundException;
import com.cosmocats.marketplace.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(PostgreSqlTestConfiguration.class)
@Transactional
class DatabaseIntegrationTest {
    @Autowired private ProductService products;
    @Autowired private CategoryService categories;
    @Autowired private OrderService orders;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private JdbcTemplate jdbc;

    // Test case: Products survive a service read and support create, update and delete in PostgreSQL.
    @Test
    void test_productCrud_persistsChanges_whenUsingPostgreSql() {
        Product created = products.create(new Product(null, "Cosmic milk", new BigDecimal("3.50")));
        assertEquals(created, products.findById(created.id()), "The saved product should be read from PostgreSQL");
        assertEquals(1L, products.count(), "The database should contain one product");

        Product updated = products.update(created.id(),
                new Product(null, "Galaxy milk", new BigDecimal("4.00")));
        assertEquals("Galaxy milk", updated.name(), "The database should store the replacement name");
        assertEquals(List.of(updated), products.findPage(0, 10), "Pagination should read the updated product");

        products.delete(created.id());
        assertThrows(ProductNotFoundException.class, () -> products.findById(created.id()),
                "Deleted products should not be found");
    }

    // Test case: Categories have their own repository-backed CRUD.
    @Test
    void test_categoryCrud_persistsChanges_whenUsingPostgreSql() {
        Category created = categories.create(new Category(null, "Food"));
        assertEquals(created, categories.findById(created.id()), "The saved category should be readable");
        assertTrue(categories.findAll().contains(created), "The category should appear in the list");

        Category updated = categories.update(created.id(), new Category(null, "Supplies"));
        assertEquals("Supplies", categories.findById(created.id()).name(),
                "Updating should change the category name");

        categories.delete(updated.id());
        assertThrows(CategoryNotFoundException.class, () -> categories.findById(updated.id()),
                "Deleted categories should not be found");
    }

    // Test case: A product can be attached to a category and the relation is stored.
    @Test
    void test_productCategory_persistsRelation_whenCategoryExists() {
        Category category = categories.create(new Category(null, "Food"));
        Product created = products.create(new Product(null, "Cosmic milk",
                new BigDecimal("3.50"), category.id()));

        assertEquals(category.id(), products.findById(created.id()).categoryId(),
                "The product should retain its category ID");
    }

    // Test case: The migration prevents duplicate product names within one category.
    @Test
    void test_productName_rejectsDuplicate_whenCategoryIsSame() {
        Category category = categories.create(new Category(null, "Food"));
        products.create(new Product(null, "Cosmic milk", new BigDecimal("3.50"), category.id()));

        assertThrows(DataIntegrityViolationException.class,
                () -> {
                    products.create(new Product(null, "Cosmic milk", new BigDecimal("4.00"), category.id()));
                    productRepository.flush();
                },
                "The unique index should reject a duplicate name within the same category");
    }

    // Test case: An order stores product links and the price at purchase time.
    @Test
    void test_orderCrud_persistsItemsAndTotal_whenUsingPostgreSql() {
        Product first = products.create(new Product(null, "Star map", new BigDecimal("5.00")));
        Product second = products.create(new Product(null, "Moon dust", new BigDecimal("2.00")));
        Order created = orders.create(new Order(null, List.of(first, second), BigDecimal.ZERO));

        assertEquals(new BigDecimal("7.00"), created.total(), "The order total should equal its product prices");
        assertEquals(2, orders.findById(created.id()).products().size(),
                "The order should store both product links");
        assertTrue(orders.findAll().stream().anyMatch(order -> order.id().equals(created.id())),
                "The order should appear in the list");

        products.update(first.id(), new Product(null, "Star map", new BigDecimal("9.00")));
        assertEquals(new BigDecimal("5.00"), orders.findById(created.id()).products().get(0).price(),
                "The old order should retain its purchase-time price");

        Order updated = orders.update(created.id(), new Order(null, List.of(second), BigDecimal.ZERO));
        assertEquals(new BigDecimal("2.00"), updated.total(), "Replacing order items should recalculate the total");
        assertEquals(1, orderRepository.findById(created.id()).orElseThrow().getItems().size(),
                "The database should retain only the replacement order item");

        orders.delete(created.id());
        assertThrows(OrderNotFoundException.class, () -> orders.findById(created.id()),
                "Deleted orders should not be found");
    }

    // Test case: Liquibase applied both schema and index migrations.
    @Test
    void test_liquibase_createsIndexesAndSequences_whenApplicationStarts() {
        Integer applied = jdbc.queryForObject(
                "SELECT COUNT(*) FROM databasechangelog WHERE id IN ('001-create-schema', '002-add-indexes-and-constraints')",
                Integer.class);
        Integer indexes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes WHERE schemaname = current_schema() "
                        + "AND indexname IN ('idx_product_name', 'uq_product_category_name')",
                Integer.class);
        Integer sequences = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pg_sequences WHERE schemaname = current_schema() "
                        + "AND sequencename IN ('category_id_seq', 'product_id_seq', 'customer_order_id_seq')",
                Integer.class);

        assertEquals(2, applied, "Liquibase should apply both versioned changesets");
        assertEquals(2, indexes, "The search and uniqueness indexes should exist");
        assertEquals(3, sequences, "The three main tables should have ID sequences");
    }
}
