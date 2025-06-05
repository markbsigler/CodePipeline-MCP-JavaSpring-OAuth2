package com.codepipeline.mcp.dto;

import com.codepipeline.mcp.model.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private String id;
    private String taskId;
    private String type;
    private String status;
    private String componentType;
    private String componentName;
    private String componentExtension;
    private String componentVersion;
    private String componentLastAction;
    private String componentLastActionDateTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskDto fromEntity(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .taskId(task.getTaskId())
                .type(task.getType())
                .status(task.getStatus())
                .componentType(task.getComponentType())
                .componentName(task.getComponentName())
                .componentExtension(task.getComponentExtension())
                .componentVersion(task.getComponentVersion())
                .componentLastAction(task.getComponentLastAction())
                .componentLastActionDateTime(task.getComponentLastActionDateTime())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public static Task toEntity(TaskDto dto) {
        return Task.builder()
                .id(dto.getId())
                .taskId(dto.getTaskId())
                .type(dto.getType())
                .status(dto.getStatus())
                .componentType(dto.getComponentType())
                .componentName(dto.getComponentName())
                .componentExtension(dto.getComponentExtension())
                .componentVersion(dto.getComponentVersion())
                .componentLastAction(dto.getComponentLastAction())
                .componentLastActionDateTime(dto.getComponentLastActionDateTime())
                .build();
    }
}
