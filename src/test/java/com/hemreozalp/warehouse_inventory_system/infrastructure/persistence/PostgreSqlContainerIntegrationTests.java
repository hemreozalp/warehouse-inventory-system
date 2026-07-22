package com.hemreozalp.warehouse_inventory_system.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PostgreSqlContainerIntegrationTests {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("warehouse_inventory_test")
            .withUsername("warehouse_user")
            .withPassword("warehouse_password");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayMigrationsApplyCleanlyOnPostgreSql() {
        String latestVersion = jdbcTemplate.queryForObject("""
                select version
                from flyway_schema_history
                where success = true
                order by installed_rank desc
                limit 1
                """, String.class);

        assertEquals("7", latestVersion);
        assertColumnExists("stock_movements", "idempotency_key");
        assertColumnExists("stock_movements", "created_by");
        assertColumnExists("stock_transfers", "idempotency_key");
        assertColumnExists("stock_transfers", "created_by");
        assertIndexExists("ux_stock_movements_idempotency_key");
        assertIndexExists("ux_stock_transfers_idempotency_key");
    }

    private void assertColumnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.columns
                where table_schema = 'public'
                  and table_name = ?
                  and column_name = ?
                """, Integer.class, tableName, columnName);

        assertEquals(1, count);
    }

    private void assertIndexExists(String indexName) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*)
                from pg_indexes
                where schemaname = 'public'
                  and indexname = ?
                """, Integer.class, indexName);

        assertEquals(1, count);
    }
}
