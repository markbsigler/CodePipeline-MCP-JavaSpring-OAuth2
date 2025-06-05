package com.codepipeline.mcp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeployRequest {
    private String level;
    private String environment;
    private String description;
    private boolean autoDeploy;
    private String runtimeConfiguration;
}
