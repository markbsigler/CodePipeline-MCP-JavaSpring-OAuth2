package com.codepipeline.mcp.controller;

import com.codepipeline.mcp.dto.AssignmentDto;
import com.codepipeline.mcp.dto.TaskDto;
import com.codepipeline.mcp.service.AssignmentService;
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
@RequestMapping("/ispw/{srid}/assignments")
@RequiredArgsConstructor
@Tag(name = "Assignments", description = "Operations related to ISPW assignments")
@SecurityRequirement(name = "bearerAuth")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all assignments for a given SRID")
    public ResponseEntity<List<AssignmentDto>> getAssignments(
            @PathVariable String srid,
            @RequestParam(required = false) String application,
            @RequestParam(required = false) String status) {
        
        List<AssignmentDto> assignments = assignmentService.getAssignments(srid, application, status);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{assignmentId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get an assignment by ID")
    public ResponseEntity<AssignmentDto> getAssignment(
            @PathVariable String srid,
            @PathVariable String assignmentId) {
        
        AssignmentDto assignment = assignmentService.getAssignment(srid, assignmentId);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/{assignmentId}/tasks")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get tasks for an assignment")
    public ResponseEntity<List<TaskDto>> getAssignmentTasks(
            @PathVariable String srid,
            @PathVariable String assignmentId) {
        
        List<TaskDto> tasks = assignmentService.getAssignmentTasks(srid, assignmentId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create a new assignment")
    public ResponseEntity<AssignmentDto> createAssignment(
            @PathVariable String srid,
            @Valid @RequestBody AssignmentDto assignmentDto) {
        
        AssignmentDto createdAssignment = assignmentService.createAssignment(srid, assignmentDto);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAssignment.getAssignmentId())
                .toUri();
                
        return ResponseEntity.created(location).body(createdAssignment);
    }

    @PutMapping("/{assignmentId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update an existing assignment")
    public ResponseEntity<AssignmentDto> updateAssignment(
            @PathVariable String srid,
            @PathVariable String assignmentId,
            @Valid @RequestBody AssignmentDto assignmentDto) {
        
        assignmentDto.setAssignmentId(assignmentId);
        AssignmentDto updatedAssignment = assignmentService.updateAssignment(srid, assignmentId, assignmentDto);
        return ResponseEntity.ok(updatedAssignment);
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete an assignment")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable String srid,
            @PathVariable String assignmentId) {
        
        assignmentService.deleteAssignment(srid, assignmentId);
        return ResponseEntity.noContent().build();
    }
}
