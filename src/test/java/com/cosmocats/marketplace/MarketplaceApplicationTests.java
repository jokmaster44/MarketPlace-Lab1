package com.cosmocats.marketplace;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cosmocats.marketplace.integration.PostgreSqlTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgreSqlTestConfiguration.class)
class MarketplaceApplicationTests {
    @Autowired
    private ApplicationContext context;

    // Test case: Spring starts with PostgreSQL and Liquibase configured.
    @Test
    void test_applicationContextLoads_whenDatabaseIsAvailable() {
        assertNotNull(context, "Spring should create the application context with PostgreSQL");
    }
}
