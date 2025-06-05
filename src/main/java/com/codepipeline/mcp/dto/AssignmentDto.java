package com.codepipeline.mcp.dto;

import com.codepipeline.mcp.model.Assignment;
import com.codepipeline.mcp.model.Task;
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
public class AssignmentDto {
    private String id;
    private String assignmentId;
    private String srid;
    private String application;
    private String stream;
    private String owner;
    private String status;
    private String releaseId;
    private String setid;
    private String level;
    private List<TaskDto> tasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AssignmentDto fromEntity(Assignment assignment) {
        return AssignmentDto.builder()
                .id(assignment.getId())
                .assignmentId(assignment.getAssignmentId())
                .srid(assignment.getSrid())
                .application(assignment.getApplication())
                .stream(assignment.getStream())
                .owner(assignment.getOwner())
                .status(assignment.getStatus())
                .releaseId(assignment.getReleaseId())
                .setid(assignment.getSetid())
                .level(assignment.getLevel())
                .tasks(assignment.getTasks().stream()
                        .map(TaskDto::fromEntity)
                        .collect(Collectors.toList()))
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }

    public static Assignment toEntity(AssignmentDto dto) {
        Assignment assignment = new Assignment();
        assignment.setId(dto.getId());
        assignment.setAssignmentId(dto.getAssignmentId());
        assignment.setSrid(dto.getSrid());
        assignment.setApplication(dto.getApplication());
        assignment.setStream(dto.getStream());
        assignment.setOwner(dto.getOwner());
        assignment.setStatus(dto.getStatus());
        assignment.setReleaseId(dto.getReleaseId());
        assignment.setSetid(dto.getSetid());
        assignment.setLevel(dto.getLevel());
        
        if (dto.getTasks() != null) {
            dto.getTasks().forEach(taskDto -> {
                Task task = TaskDto.toEntity(taskDto);
                assignment.addTask(task);
            });
        }
        
        return assignment;
    }
}
