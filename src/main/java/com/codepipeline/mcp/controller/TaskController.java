package com.codepipeline.mcp.controller;

import com.codepipeline.mcp.dto.TaskDto;
import com.codepipeline.mcp.service.TaskService;
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
@RequestMapping("/ispw/{srid}/assignments/{assignmentId}/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Operations related to tasks within assignments")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all tasks for an assignment")
    public ResponseEntity<List<TaskDto>> getTasks(
            @PathVariable String srid,
            @PathVariable String assignmentId) {
        
        List<TaskDto> tasks = taskService.getTasks(srid, assignmentId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get a task by ID")
    public ResponseEntity<TaskDto> getTask(
            @PathVariable String srid,
            @PathVariable String assignmentId,
            @PathVariable String taskId) {
        
        TaskDto task = taskService.getTask(srid, assignmentId, taskId);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create a new task in an assignment")
    public ResponseEntity<TaskDto> createTask(
            @PathVariable String srid,
            @PathVariable String assignmentId,
            @Valid @RequestBody TaskDto taskDto) {
        
        TaskDto createdTask = taskService.createTask(srid, assignmentId, taskDto);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTask.getTaskId())
                .toUri();
                
        return ResponseEntity.created(location).body(createdTask);
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update an existing task")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable String srid,
            @PathVariable String assignmentId,
            @PathVariable String taskId,
            @Valid @RequestBody TaskDto taskDto) {
        
        taskDto.setTaskId(taskId);
        TaskDto updatedTask = taskService.updateTask(srid, assignmentId, taskId, taskDto);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(
            @PathVariable String srid,
            @PathVariable String assignmentId,
            @PathVariable String taskId) {
        
        taskService.deleteTask(srid, assignmentId, taskId);
        return ResponseEntity.noContent().build();
    }
}
