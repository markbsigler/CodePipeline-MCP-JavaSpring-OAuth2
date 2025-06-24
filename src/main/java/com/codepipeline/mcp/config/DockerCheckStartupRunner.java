package com.codepipeline.mcp.config;

import com.codepipeline.mcp.util.DockerUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DockerCheckStartupRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        if (!DockerUtils.isDockerRunning()) {
            System.err.println("[FATAL] Docker engine is not running. Please start Docker before running the application.");
            System.exit(1);
        }
    }
}
