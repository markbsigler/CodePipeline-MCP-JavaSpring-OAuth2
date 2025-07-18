package com.codepipeline.mcp;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * Base class for integration tests that require a PostgreSQL database.
 * Starts a single PostgreSQL container that can be reused across test classes.
 */

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
public abstract class BaseIntegrationTest {

    private static final String POSTGRES_IMAGE = "postgres:14-alpine";
    private static final String TEST_DB_NAME = "testdb";
    private static final String TEST_DB_USERNAME = "testuser";
    private static final String TEST_DB_PASSWORD = "testpass";
    private static final int POSTGRES_PORT = 5432;

    @Container
    protected static final PostgreSQLContainer<?> postgreSQLContainer = 
        new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
            .withDatabaseName(TEST_DB_NAME)
            .withUsername(TEST_DB_USERNAME)
            .withPassword(TEST_DB_PASSWORD)
            .withExposedPorts(POSTGRES_PORT)
            .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("POSTGRES"))
            .withStartupTimeout(Duration.ofSeconds(120))
            .waitingFor(Wait.forListeningPort()
                .withStartupTimeout(Duration.ofSeconds(30)));

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        if (!isContainerRunning()) {
            log.error("PostgreSQL container is not running! Tests will likely fail.");
            return;
        }
        
        // Configure the JDBC URL with the mapped port
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?currentSchema=public",
            postgreSQLContainer.getHost(),
            postgreSQLContainer.getMappedPort(POSTGRES_PORT),
            TEST_DB_NAME);
            
        log.info("Configuring test datasource: {}", jdbcUrl);
        
        // Configure the test database
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        
        // JPA/Hibernate properties
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.properties.hibernate.dialect", 
            () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation", 
            () -> "true");
            
        // Test properties
        registry.add("spring.test.database.replace", () -> "none");
        registry.add("spring.flyway.enabled", () -> "false");
        
        // Log the configuration
        log.info("Test database configured with URL: {}", jdbcUrl);
    }

    @BeforeAll
    static void beforeAll() {
        if (!com.codepipeline.mcp.util.DockerUtils.isDockerRunning()) {
            throw new RuntimeException("Docker engine is not running. Please start Docker to run integration tests.");
        }
        log.info("Starting PostgreSQL container...");
        if (!isContainerRunning()) {
            try {
                postgreSQLContainer.start();
                log.info("PostgreSQL container started at: {}", 
                    postgreSQLContainer.getJdbcUrl());
            } catch (Exception e) {
                log.error("Failed to start PostgreSQL container: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to start PostgreSQL container", e);
            }
        }
    }

    @AfterAll
    static void afterAll() {
        // Let Testcontainers handle container cleanup
        // We don't stop the container here to allow for reuse between test classes
    }
    
    protected static boolean isContainerRunning() {
        return postgreSQLContainer != null && postgreSQLContainer.isRunning();
    }
}
