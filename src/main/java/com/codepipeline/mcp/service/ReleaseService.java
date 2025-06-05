package com.codepipeline.mcp.service;

import com.codepipeline.mcp.dto.DeployRequest;
import com.codepipeline.mcp.dto.ReleaseDto;
import com.codepipeline.mcp.exception.ResourceNotFoundException;
import com.codepipeline.mcp.model.Release;
import com.codepipeline.mcp.model.ReleaseSet;
import com.codepipeline.mcp.repository.ReleaseRepository;
import com.codepipeline.mcp.repository.ReleaseSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codepipeline.mcp.dto.ReleaseSetDto;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReleaseService {

    private final ReleaseRepository releaseRepository;
    private final ReleaseSetRepository releaseSetRepository;

    @Transactional(readOnly = true)
    public List<ReleaseDto> getReleases(String srid, String application, String status) {
        List<Release> releases;
        
        if ((application == null || application.isEmpty()) && (status == null || status.isEmpty())) {
            releases = releaseRepository.findBySrid(srid);
        } else {
            releases = releaseRepository.findBySridAndFilters(
                    srid,
                    (application != null && !application.isEmpty()) ? application : null,
                    (status != null && !status.isEmpty()) ? status : null
            );
        }
        
        // Fetch release sets for each release to avoid N+1 problem
        List<ReleaseDto> releaseDtos = releases.stream()
                .map(ReleaseDto::fromEntity)
                .collect(Collectors.toList());
        
        // Populate release sets for each release
        releaseDtos.forEach(releaseDto -> {
            List<ReleaseSetDto> sets = releaseSetRepository.findByReleaseId(releaseDto.getReleaseId()).stream()
                    .map(ReleaseSetDto::fromEntity)
                    .collect(Collectors.toList());
            releaseDto.setSets(sets);
        });
        
        return releaseDtos;
    }

    @Transactional(readOnly = true)
    public ReleaseDto getRelease(String srid, String releaseId) {
        Release release = releaseRepository.findByReleaseIdAndSrid(releaseId, srid)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));
        
        return ReleaseDto.fromEntity(release);
    }

    @Transactional
    public ReleaseDto createRelease(String srid, ReleaseDto releaseDto) {
        if (releaseRepository.existsByReleaseIdAndSrid(releaseDto.getReleaseId(), srid)) {
            throw new IllegalArgumentException("Release with id " + releaseDto.getReleaseId() + " already exists");
        }
        
        Release release = ReleaseDto.toEntity(releaseDto);
        release.setSrid(srid);
        
        Release savedRelease = releaseRepository.save(release);
        return ReleaseDto.fromEntity(savedRelease);
    }

    @Transactional
    public ReleaseDto updateRelease(String srid, String releaseId, ReleaseDto releaseDto) {
        Release existingRelease = releaseRepository.findByReleaseIdAndSrid(releaseId, srid)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));
        
        // Update fields from DTO
        existingRelease.setApplication(releaseDto.getApplication());
        existingRelease.setStream(releaseDto.getStream());
        existingRelease.setOwner(releaseDto.getOwner());
        existingRelease.setStatus(releaseDto.getStatus());
        existingRelease.setDescription(releaseDto.getDescription());
        
        // Update sets if provided
        if (releaseDto.getSets() != null) {
            // Clear existing sets
            existingRelease.getSets().clear();
            
            // Add new sets
            releaseDto.getSets().forEach(setDto -> {
                ReleaseSet releaseSet = ReleaseSetDto.toEntity(setDto);
                existingRelease.addSet(releaseSet);
            });
        }
        
        Release updatedRelease = releaseRepository.save(existingRelease);
        return ReleaseDto.fromEntity(updatedRelease);
    }

    @Transactional
    public void deleteRelease(String srid, String releaseId) {
        Release release = releaseRepository.findByReleaseIdAndSrid(releaseId, srid)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));
        
        releaseRepository.delete(release);
    }

    @Transactional
    public ReleaseDto deployRelease(String srid, String releaseId, DeployRequest deployRequest) {
        Release release = releaseRepository.findByReleaseIdAndSrid(releaseId, srid)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));
        
        // Update release status
        release.setStatus("DEPLOY_IN_PROGRESS");
        
        // Create a new release set for this deployment
        ReleaseSet releaseSet = ReleaseSet.builder()
                .setId("SET-" + System.currentTimeMillis())
                .status("IN_PROGRESS")
                .owner(deployRequest.getEnvironment() + "-deployer")
                .description("Deployment to " + deployRequest.getEnvironment() + " - " + deployRequest.getDescription())
                .deployedBy("system")
                .deploymentStatus("IN_PROGRESS")
                .build();
        
        release.addSet(releaseSet);
        
        Release updatedRelease = releaseRepository.save(release);
        
        // In a real implementation, you would trigger the actual deployment process here
        // and update the status asynchronously
        
        return ReleaseDto.fromEntity(updatedRelease);
    }
}
