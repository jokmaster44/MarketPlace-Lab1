package com.cosmocats.marketplace;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class MarketplaceApplicationTests {

    @Autowired
    private ApplicationContext context;

    // Test case: Spring starts with the current application configuration.
    @Test
    void test_applicationContextLoads_whenConfigurationIsValid() {
        assertNotNull(context, "Spring should create the application context");
    }
}
