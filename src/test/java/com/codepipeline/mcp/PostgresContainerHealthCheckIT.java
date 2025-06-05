package com.codepipeline.mcp;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify PostgreSQL container and application health.
 * This test starts a PostgreSQL container and verifies the application can connect to it.
 */
@Slf4j
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostgresContainerHealthCheckIT {

    private static final String POSTGRES_IMAGE = "postgres:14-alpine";
    private static final String TEST_DB_NAME = "testdb";
    private static final String TEST_DB_USERNAME = "testuser";
    private static final String TEST_DB_PASSWORD = "testpass";
    private static final int POSTGRES_PORT = 5432;

    @LocalServerPort
    private int port;

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer = 
        new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName(TEST_DB_NAME)
            .withUsername(TEST_DB_USERNAME)
            .withPassword(TEST_DB_PASSWORD)
            .withExposedPorts(POSTGRES_PORT)
            .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("POSTGRES"))
            .withStartupTimeout(Duration.ofSeconds(120))
            .waitingFor(Wait.forListeningPort()
                .withStartupTimeout(Duration.ofSeconds(30)));

    @BeforeAll
    static void beforeAll() {
        // Set system properties for test container
        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
        System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
        
        log.info("Test container started with JDBC URL: {}", postgreSQLContainer.getJdbcUrl());
    }

    @Test
    void testPostgreSQLContainerIsRunning() {
        assertThat(postgreSQLContainer.isRunning()).isTrue();
        
        // Verify container details
        log.info("PostgreSQL Container ID: {}", postgreSQLContainer.getContainerId());
        log.info("PostgreSQL Database: {}", postgreSQLContainer.getDatabaseName());
        log.info("PostgreSQL Username: {}", postgreSQLContainer.getUsername());
        log.info("PostgreSQL JDBC URL: {}", postgreSQLContainer.getJdbcUrl());
        log.info("PostgreSQL Mapped Port: {}", postgreSQLContainer.getMappedPort(POSTGRES_PORT));
    }

    @Test
    void testApplicationHealthEndpoint() {
        RestTemplate restTemplate = new RestTemplate();
        String healthUrl = "http://localhost:" + port + "/actuator/health";
        
        log.info("Testing health endpoint at: {}", healthUrl);
        
        ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
        
        log.info("Health check response: {}", response.getBody());
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("UP");
    }
}
