package com.codepipeline.mcp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hello")
@RequiredArgsConstructor
@Tag(name = "Hello", description = "Hello World API")
public class HelloController {

    @GetMapping
    @Operation(summary = "Public hello endpoint")
    public ResponseEntity<String> publicHello() {
        return ResponseEntity.ok("Hello, World! This is a public endpoint.");
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
        summary = "User hello endpoint",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<String> userHello(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return ResponseEntity.ok("Hello, " + username + "! You have USER role.");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Admin hello endpoint",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<String> adminHello(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return ResponseEntity.ok("Hello, " + username + "! You have ADMIN role.");
    }
}
