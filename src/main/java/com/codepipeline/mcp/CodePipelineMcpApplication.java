package com.codepipeline.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodePipelineMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodePipelineMcpApplication.class, args);
    }
}
