package com.codepipeline.mcp.controller;

import com.codepipeline.mcp.dto.DeployRequest;
import com.codepipeline.mcp.dto.ReleaseDto;
import com.codepipeline.mcp.service.ReleaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/ispw/{srid}/releases")
@RequiredArgsConstructor
@Tag(name = "Releases", description = "Operations related to ISPW releases")
@SecurityRequirement(name = "bearerAuth")
public class ReleaseController {

    private final ReleaseService releaseService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all releases for a given SRID")
    public ResponseEntity<List<ReleaseDto>> getReleases(
            @PathVariable String srid,
            @RequestParam(required = false) String application,
            @RequestParam(required = false) String status) {
        
        List<ReleaseDto> releases = releaseService.getReleases(srid, application, status);
        return ResponseEntity.ok(releases);
    }

    @GetMapping("/{releaseId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get a release by ID")
    public ResponseEntity<ReleaseDto> getRelease(
            @PathVariable String srid,
            @PathVariable String releaseId) {
        
        ReleaseDto release = releaseService.getRelease(srid, releaseId);
        return ResponseEntity.ok(release);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create a new release")
    public ResponseEntity<ReleaseDto> createRelease(
            @PathVariable String srid,
            @Valid @RequestBody ReleaseDto releaseDto) {
        
        ReleaseDto createdRelease = releaseService.createRelease(srid, releaseDto);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRelease.getReleaseId())
                .toUri();
                
        return ResponseEntity.created(location).body(createdRelease);
    }

    @PutMapping("/{releaseId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update an existing release")
    public ResponseEntity<ReleaseDto> updateRelease(
            @PathVariable String srid,
            @PathVariable String releaseId,
            @Valid @RequestBody ReleaseDto releaseDto) {
        
        releaseDto.setReleaseId(releaseId);
        ReleaseDto updatedRelease = releaseService.updateRelease(srid, releaseId, releaseDto);
        return ResponseEntity.ok(updatedRelease);
    }

    @DeleteMapping("/{releaseId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete a release")
    public ResponseEntity<Void> deleteRelease(
            @PathVariable String srid,
            @PathVariable String releaseId) {
        
        releaseService.deleteRelease(srid, releaseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{releaseId}/deploy")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Deploy a release")
    public ResponseEntity<ReleaseDto> deployRelease(
            @PathVariable String srid,
            @PathVariable String releaseId,
            @Valid @RequestBody DeployRequest deployRequest) {
        
        ReleaseDto deployedRelease = releaseService.deployRelease(srid, releaseId, deployRequest);
        return ResponseEntity.ok(deployedRelease);
    }
}
