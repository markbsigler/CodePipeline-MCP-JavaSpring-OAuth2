package com.codepipeline.mcp.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Map;

@TestConfiguration
@ActiveProfiles("test")
public class TestConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> new Jwt(
            "test-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "none"),
            Map.of(
                "sub", "test-user",
                "email", "test@example.com",
                "preferred_username", "testuser"
            )
        );
    }
}
