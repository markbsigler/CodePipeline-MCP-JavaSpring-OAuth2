package com.codepipeline.mcp.service;

import com.codepipeline.mcp.dto.DeployRequest;
import com.codepipeline.mcp.dto.ReleaseSetDto;
import com.codepipeline.mcp.exception.ResourceNotFoundException;
import com.codepipeline.mcp.model.Release;
import com.codepipeline.mcp.model.ReleaseSet;
import com.codepipeline.mcp.repository.ReleaseRepository;
import com.codepipeline.mcp.repository.ReleaseSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReleaseSetService {

    private final ReleaseSetRepository releaseSetRepository;
    private final ReleaseRepository releaseRepository;

    @Transactional(readOnly = true)
    public List<ReleaseSetDto> getReleaseSets(String srid) {
        return releaseSetRepository.findByReleaseSrid(srid).stream()
                .map(ReleaseSetDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReleaseSetDto getReleaseSet(String srid, String setId) {
        ReleaseSet releaseSet = releaseSetRepository.findBySetIdAndReleaseSrid(setId, srid)
                .orElseThrow(() -> new ResourceNotFoundException("Release set not found with id: " + setId));
        
        return ReleaseSetDto.fromEntity(releaseSet);
    }

    @Transactional
    public ReleaseSetDto createReleaseSet(String srid, String releaseId, ReleaseSetDto releaseSetDto) {
        // Get the release first
        Release release = releaseRepository.findByReleaseIdAndSrid(releaseId, srid)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));
        
        if (releaseSetRepository.existsBySetIdAndReleaseId(releaseSetDto.getSetId(), releaseId)) {
            throw new IllegalArgumentException("Release set with id " + releaseSetDto.getSetId() + " already exists in release " + releaseId);
        }
        
        ReleaseSet releaseSet = ReleaseSetDto.toEntity(releaseSetDto);
        release.addSet(releaseSet);
        
        // Save the release to cascade the save to releaseSet
        releaseRepository.save(release);
        
        return ReleaseSetDto.fromEntity(releaseSet);
    }

    @Transactional
    public ReleaseSetDto updateReleaseSet(String srid, String releaseId, String setId, ReleaseSetDto releaseSetDto) {
        // Verify release exists
        if (!releaseRepository.existsByReleaseIdAndSrid(releaseId, srid)) {
            throw new ResourceNotFoundException("Release not found with id: " + releaseId);
        }
        
        ReleaseSet existingReleaseSet = releaseSetRepository.findBySetIdAndReleaseId(setId, releaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Release set not found with id: " + setId));
        
        // Update fields from DTO
        existingReleaseSet.setStatus(releaseSetDto.getStatus());
        existingReleaseSet.setOwner(releaseSetDto.getOwner());
        existingReleaseSet.setDescription(releaseSetDto.getDescription());
        existingReleaseSet.setDeployedBy(releaseSetDto.getDeployedBy());
        existingReleaseSet.setDeployedAt(releaseSetDto.getDeployedAt());
        existingReleaseSet.setDeploymentStatus(releaseSetDto.getDeploymentStatus());
        
        ReleaseSet updatedReleaseSet = releaseSetRepository.save(existingReleaseSet);
        return ReleaseSetDto.fromEntity(updatedReleaseSet);
    }

    @Transactional
    public void deleteReleaseSet(String srid, String releaseId, String setId) {
        if (!releaseRepository.existsByReleaseIdAndSrid(releaseId, srid)) {
            throw new ResourceNotFoundException("Release not found with id: " + releaseId);
        }
        
        ReleaseSet releaseSet = releaseSetRepository.findBySetIdAndReleaseId(setId, releaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Release set not found with id: " + setId));
        
        releaseSetRepository.delete(releaseSet);
    }

    @Transactional
    public ReleaseSetDto deployReleaseSet(String srid, String setId, DeployRequest deployRequest) {
        ReleaseSet releaseSet = releaseSetRepository.findBySetIdAndReleaseSrid(setId, srid)
                .orElseThrow(() -> new ResourceNotFoundException("Release set not found with id: " + setId));
        
        // Update release set status
        releaseSet.setStatus("DEPLOY_IN_PROGRESS");
        releaseSet.setDeployedBy("system");
        releaseSet.setDeployedAt(LocalDateTime.now());
        releaseSet.setDeploymentStatus("IN_PROGRESS");
        
        ReleaseSet updatedReleaseSet = releaseSetRepository.save(releaseSet);
        
        // In a real implementation, you would trigger the actual deployment process here
        // and update the status asynchronously
        
        return ReleaseSetDto.fromEntity(updatedReleaseSet);
    }
}
