package com.codepipeline.mcp.util;

import java.io.IOException;

public class DockerUtils {
    public static boolean isDockerRunning() {
        try {
            Process process = new ProcessBuilder("docker", "info").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
