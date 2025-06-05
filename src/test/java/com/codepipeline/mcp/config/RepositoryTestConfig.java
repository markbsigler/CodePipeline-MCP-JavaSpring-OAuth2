package com.codepipeline.mcp.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@TestConfiguration
@EnableAutoConfiguration
@EntityScan("com.codepipeline.mcp.model")
@EnableJpaRepositories("com.codepipeline.mcp.repository")
@EnableTransactionManagement
@ActiveProfiles("test")
public class RepositoryTestConfig {
}
