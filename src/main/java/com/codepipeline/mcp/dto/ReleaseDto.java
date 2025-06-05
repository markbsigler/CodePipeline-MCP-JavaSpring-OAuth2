package com.codepipeline.mcp.dto;

import com.codepipeline.mcp.model.Release;
import com.codepipeline.mcp.model.ReleaseSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseDto {
    private String id;
    private String releaseId;
    private String srid;
    private String application;
    private String stream;
    private String owner;
    private String status;
    private String description;
    private List<ReleaseSetDto> sets;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReleaseDto fromEntity(Release release) {
        return ReleaseDto.builder()
                .id(release.getId())
                .releaseId(release.getReleaseId())
                .srid(release.getSrid())
                .application(release.getApplication())
                .stream(release.getStream())
                .owner(release.getOwner())
                .status(release.getStatus())
                .description(release.getDescription())
                .sets(release.getSets().stream()
                        .map(ReleaseSetDto::fromEntity)
                        .collect(Collectors.toList()))
                .createdAt(release.getCreatedAt())
                .updatedAt(release.getUpdatedAt())
                .build();
    }

    public static Release toEntity(ReleaseDto dto) {
        Release release = new Release();
        release.setId(dto.getId());
        release.setReleaseId(dto.getReleaseId());
        release.setSrid(dto.getSrid());
        release.setApplication(dto.getApplication());
        release.setStream(dto.getStream());
        release.setOwner(dto.getOwner());
        release.setStatus(dto.getStatus());
        release.setDescription(dto.getDescription());
        
        if (dto.getSets() != null) {
            dto.getSets().forEach(setDto -> {
                ReleaseSet releaseSet = ReleaseSetDto.toEntity(setDto);
                release.addSet(releaseSet);
            });
        }
        
        return release;
    }
}
