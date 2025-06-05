package com.codepipeline.mcp.controller;

import com.codepipeline.mcp.dto.DeployRequest;
import com.codepipeline.mcp.dto.ReleaseSetDto;
import com.codepipeline.mcp.service.ReleaseSetService;
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
@RequestMapping("/ispw/{srid}/sets")
@RequiredArgsConstructor
@Tag(name = "Release Sets", description = "Operations related to release sets")
@SecurityRequirement(name = "bearerAuth")
public class ReleaseSetController {

    private final ReleaseSetService releaseSetService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all release sets for a given SRID")
    public ResponseEntity<List<ReleaseSetDto>> getReleaseSets(
            @PathVariable String srid) {
        
        List<ReleaseSetDto> releaseSets = releaseSetService.getReleaseSets(srid);
        return ResponseEntity.ok(releaseSets);
    }

    @GetMapping("/{setId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get a release set by ID")
    public ResponseEntity<ReleaseSetDto> getReleaseSet(
            @PathVariable String srid,
            @PathVariable String setId) {
        
        ReleaseSetDto releaseSet = releaseSetService.getReleaseSet(srid, setId);
        return ResponseEntity.ok(releaseSet);
    }

    @PostMapping("/{releaseId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create a new release set in a release")
    public ResponseEntity<ReleaseSetDto> createReleaseSet(
            @PathVariable String srid,
            @PathVariable String releaseId,
            @Valid @RequestBody ReleaseSetDto releaseSetDto) {
        
        ReleaseSetDto createdReleaseSet = releaseSetService.createReleaseSet(srid, releaseId, releaseSetDto);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdReleaseSet.getSetId())
                .toUri();
                
        return ResponseEntity.created(location).body(createdReleaseSet);
    }

    @PutMapping("/{releaseId}/{setId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update an existing release set")
    public ResponseEntity<ReleaseSetDto> updateReleaseSet(
            @PathVariable String srid,
            @PathVariable String releaseId,
            @PathVariable String setId,
            @Valid @RequestBody ReleaseSetDto releaseSetDto) {
        
        releaseSetDto.setSetId(setId);
        ReleaseSetDto updatedReleaseSet = releaseSetService.updateReleaseSet(srid, releaseId, setId, releaseSetDto);
        return ResponseEntity.ok(updatedReleaseSet);
    }

    @DeleteMapping("/{releaseId}/{setId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete a release set")
    public ResponseEntity<Void> deleteReleaseSet(
            @PathVariable String srid,
            @PathVariable String releaseId,
            @PathVariable String setId) {
        
        releaseSetService.deleteReleaseSet(srid, releaseId, setId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{setId}/deploy")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Deploy a release set")
    public ResponseEntity<ReleaseSetDto> deployReleaseSet(
            @PathVariable String srid,
            @PathVariable String setId,
            @Valid @RequestBody DeployRequest deployRequest) {
        
        ReleaseSetDto deployedReleaseSet = releaseSetService.deployReleaseSet(srid, setId, deployRequest);
        return ResponseEntity.ok(deployedReleaseSet);
    }
}
