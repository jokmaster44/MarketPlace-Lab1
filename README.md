# Cosmo Cats Marketplace

The product API stores data in PostgreSQL. Spring Data JPA maps Java entities to tables, and Liquibase applies versioned schema changes when the application starts.

## Run locally

Docker must be running. In this project directory:

    docker compose up -d
    .\mvnw.cmd spring-boot:run

The API is available at http://localhost:8080/api/products. For example:

    Invoke-RestMethod http://localhost:8080/api/products
    Invoke-RestMethod -Method Post http://localhost:8080/api/products -ContentType 'application/json' -Body '{"name":"Cosmic milk","price":3.50}'

Stop the application with Ctrl+C. The command "docker compose down" stops the database container and keeps its data volume.

The local Compose file uses the cosmocats database and user. Set DB_URL, DB_USER, and DB_PASSWORD to connect to another PostgreSQL instance. If port 5432 is occupied, set POSTGRES_PORT for Compose and point DB_URL to that port.

## Database design

- category: category ID and unique name.
- product: product ID, name, price, and optional category ID. Names are unique within a category.
- customer_order: order ID and total.
- order_item: links an order to a product and stores quantity and unit price at purchase time.

Liquibase's master changelog includes the initial tables and a separate change for indexes and uniqueness constraints. Hibernate validates the migrated schema instead of creating tables.

The existing product endpoints remain available. ProductRequest accepts an optional categoryId. Category and order CRUD are implemented in CategoryService and OrderService through Spring Data repositories; no HTTP endpoints for those services are part of this lab's requirements.

## Verify

    .\mvnw.cmd clean verify

This runs unit and controller tests, starts PostgreSQL with Testcontainers for integration tests, and creates target/site/jacoco/index.html. Maven fails if service package line coverage is below 60% or overall line coverage is below 80%. Docker must be running for the integration tests.
