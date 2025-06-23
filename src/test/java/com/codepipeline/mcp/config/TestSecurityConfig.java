package com.codepipeline.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

@Configuration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtToken -> new JwtAuthenticationToken(
                        jwtToken,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                        jwtToken.getClaimAsString("preferred_username")
                    ))
                )
            );
        return http.build();
    }

    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() {
        return token -> new Jwt(
            token,
            Instant.now(),
            Instant.now().plusSeconds(300),
            Map.of("alg", "RS256"),
            Map.of(
                "sub", "test-user",
                "preferred_username", "testuser@example.com",
                "scope", "read write",
                "email_verified", true,
                "name", "Test User",
                "email", "testuser@example.com"
            )
        );
    }
}
