package com.codepipeline.mcp.dto;

import com.codepipeline.mcp.model.ReleaseSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseSetDto {
    private String id;
    private String setId;
    private String status;
    private String owner;
    private String description;
    private String deployedBy;
    private LocalDateTime deployedAt;
    private String deploymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReleaseSetDto fromEntity(ReleaseSet releaseSet) {
        return ReleaseSetDto.builder()
                .id(releaseSet.getId())
                .setId(releaseSet.getSetId())
                .status(releaseSet.getStatus())
                .owner(releaseSet.getOwner())
                .description(releaseSet.getDescription())
                .deployedBy(releaseSet.getDeployedBy())
                .deployedAt(releaseSet.getDeployedAt())
                .deploymentStatus(releaseSet.getDeploymentStatus())
                .createdAt(releaseSet.getCreatedAt())
                .updatedAt(releaseSet.getUpdatedAt())
                .build();
    }

    public static ReleaseSet toEntity(ReleaseSetDto dto) {
        return ReleaseSet.builder()
                .id(dto.getId())
                .setId(dto.getSetId())
                .status(dto.getStatus())
                .owner(dto.getOwner())
                .description(dto.getDescription())
                .deployedBy(dto.getDeployedBy())
                .deployedAt(dto.getDeployedAt())
                .deploymentStatus(dto.getDeploymentStatus())
                .build();
    }
}
