package com.codepipeline.mcp.repository;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAutoConfiguration
@EntityScan("com.codepipeline.mcp.model")
@EnableJpaRepositories("com.codepipeline.mcp.repository")
@EnableTransactionManagement
@ActiveProfiles("test")
public class RepositoryTestConfig {
}
