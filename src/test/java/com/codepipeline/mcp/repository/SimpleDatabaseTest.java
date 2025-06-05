package com.codepipeline.mcp.repository;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class SimpleDatabaseTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14-alpine"))
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Test
    void testDatabaseConnection() throws SQLException {
        // Get the JDBC URL from the container
        String jdbcUrl = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        // Test the connection
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Connection successful, now test a simple query
            try (var statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT 'test' as test")) {
                
                assertThat(resultSet.next()).isTrue();
                String result = resultSet.getString("test");
                assertThat(result).isEqualTo("test");
            }
        }
    }
}
