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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContainerHealthCheckIT {

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

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeAll
    static void beforeAll() {
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
    void testApplicationHealthEndpoint() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String healthUrl = "http://localhost:" + port + "/actuator/health";
        
        log.info("Testing health endpoint at: {}", healthUrl);
        
        ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
        
        log.info("Health check response: {}", response.getBody());
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("UP");
    }
}
