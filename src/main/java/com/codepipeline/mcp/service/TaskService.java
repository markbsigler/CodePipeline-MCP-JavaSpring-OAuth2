package com.codepipeline.mcp.service;

import com.codepipeline.mcp.dto.TaskDto;
import com.codepipeline.mcp.exception.ResourceNotFoundException;
import com.codepipeline.mcp.model.Assignment;
import com.codepipeline.mcp.model.Task;
import com.codepipeline.mcp.repository.AssignmentRepository;
import com.codepipeline.mcp.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final AssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public List<TaskDto> getTasks(String srid, String assignmentId) {
        if (!assignmentRepository.existsByAssignmentIdAndSrid(assignmentId, srid)) {
            throw new ResourceNotFoundException("Assignment not found with id: " + assignmentId);
        }
        
        return taskRepository.findByAssignmentId(assignmentId).stream()
                .map(TaskDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDto getTask(String srid, String assignmentId, String taskId) {
        if (!assignmentRepository.existsByAssignmentIdAndSrid(assignmentId, srid)) {
            throw new ResourceNotFoundException("Assignment not found with id: " + assignmentId);
        }
        
        Task task = taskRepository.findByTaskIdAndAssignmentId(taskId, assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        return TaskDto.fromEntity(task);
    }

    @Transactional
    public TaskDto createTask(String srid, String assignmentId, TaskDto taskDto) {
        Assignment assignment = assignmentRepository.findByAssignmentIdAndSrid(assignmentId, srid)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        
        if (taskRepository.existsByTaskIdAndAssignmentId(taskDto.getTaskId(), assignmentId)) {
            throw new IllegalArgumentException("Task with id " + taskDto.getTaskId() + " already exists in assignment " + assignmentId);
        }
        
        Task task = TaskDto.toEntity(taskDto);
        assignment.addTask(task);
        
        Task savedTask = taskRepository.save(task);
        return TaskDto.fromEntity(savedTask);
    }

    @Transactional
    public TaskDto updateTask(String srid, String assignmentId, String taskId, TaskDto taskDto) {
        if (!assignmentRepository.existsByAssignmentIdAndSrid(assignmentId, srid)) {
            throw new ResourceNotFoundException("Assignment not found with id: " + assignmentId);
        }
        
        Task existingTask = taskRepository.findByTaskIdAndAssignmentId(taskId, assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        // Update fields from DTO
        existingTask.setType(taskDto.getType());
        existingTask.setStatus(taskDto.getStatus());
        existingTask.setComponentType(taskDto.getComponentType());
        existingTask.setComponentName(taskDto.getComponentName());
        existingTask.setComponentExtension(taskDto.getComponentExtension());
        existingTask.setComponentVersion(taskDto.getComponentVersion());
        existingTask.setComponentLastAction(taskDto.getComponentLastAction());
        existingTask.setComponentLastActionDateTime(taskDto.getComponentLastActionDateTime());
        
        Task updatedTask = taskRepository.save(existingTask);
        return TaskDto.fromEntity(updatedTask);
    }

    @Transactional
    public void deleteTask(String srid, String assignmentId, String taskId) {
        if (!assignmentRepository.existsByAssignmentIdAndSrid(assignmentId, srid)) {
            throw new ResourceNotFoundException("Assignment not found with id: " + assignmentId);
        }
        
        Task task = taskRepository.findByTaskIdAndAssignmentId(taskId, assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        taskRepository.delete(task);
    }
}
