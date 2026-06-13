package com.org.llm;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Spins up a throwaway Postgres for tests; {@code @ServiceConnection} wires the datasource
 * automatically, so Flyway and the JDBC chat-memory schema run against the container instead
 * of a locally provisioned database.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:18");
    }
}
