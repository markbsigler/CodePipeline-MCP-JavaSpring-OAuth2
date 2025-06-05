package com.codepipeline.mcp;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple test to verify PostgreSQL container starts and is accessible.
 * This test doesn't load the Spring context.
 */
@Slf4j
public class PostgresContainerTest {

    private static final String POSTGRES_IMAGE = "postgres:14-alpine";
    private static final String TEST_DB_NAME = "testdb";
    private static final String TEST_DB_USERNAME = "testuser";
    private static final String TEST_DB_PASSWORD = "testpass";
    private static final int POSTGRES_PORT = 5432;

    private static PostgreSQLContainer<?> postgresContainer;

    @BeforeAll
    static void beforeAll() {
        postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
                .withDatabaseName(TEST_DB_NAME)
                .withUsername(TEST_DB_USERNAME)
                .withPassword(TEST_DB_PASSWORD)
                .withExposedPorts(POSTGRES_PORT)
                .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("POSTGRES"))
                .withStartupTimeout(Duration.ofSeconds(120))
                .waitingFor(Wait.forListeningPort()
                        .withStartupTimeout(Duration.ofSeconds(30)));

        postgresContainer.start();
        
        log.info("PostgreSQL container started with JDBC URL: {}", postgresContainer.getJdbcUrl());
        log.info("PostgreSQL container is running: {}", postgresContainer.isRunning());
    }

    @AfterAll
    static void afterAll() {
        if (postgresContainer != null) {
            postgresContainer.stop();
            log.info("PostgreSQL container stopped");
        }
    }

    @Test
    void testPostgreSQLContainerIsRunning() {
        assertThat(postgresContainer.isRunning()).isTrue();
        
        log.info("PostgreSQL Container ID: {}", postgresContainer.getContainerId());
        log.info("PostgreSQL Database: {}", postgresContainer.getDatabaseName());
        log.info("PostgreSQL Username: {}", postgresContainer.getUsername());
        log.info("PostgreSQL JDBC URL: {}", postgresContainer.getJdbcUrl());
        log.info("PostgreSQL Mapped Port: {}", postgresContainer.getMappedPort(POSTGRES_PORT));
    }

    @Test
    void testCanConnectToPostgreSQL() throws Exception {
        // Load the PostgreSQL JDBC driver
        Class.forName("org.postgresql.Driver");
        
        // Get connection details from the container
        String jdbcUrl = postgresContainer.getJdbcUrl();
        String username = postgresContainer.getUsername();
        String password = postgresContainer.getPassword();

        log.info("Attempting to connect to PostgreSQL with URL: {}", jdbcUrl);
        
        // Set up connection properties
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        props.setProperty("ssl", "false");
        
        // Connect to the database
        try (Connection conn = DriverManager.getConnection(jdbcUrl, props);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            
            // Verify we got a result
            assertThat(rs.next()).isTrue();
            int result = rs.getInt(1);
            assertThat(result).isEqualTo(1);
            log.info("Successfully connected to PostgreSQL and executed test query");
            
            // Print some database information
            log.info("PostgreSQL Version: {}", conn.getMetaData().getDatabaseProductVersion());
            log.info("Driver Version: {}", conn.getMetaData().getDriverVersion());
        }
    }
}
