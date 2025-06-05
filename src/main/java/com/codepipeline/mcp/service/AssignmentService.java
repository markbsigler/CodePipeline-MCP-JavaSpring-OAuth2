package com.codepipeline.mcp.service;

import com.codepipeline.mcp.dto.AssignmentDto;
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
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<AssignmentDto> getAssignments(String srid, String application, String status) {
        List<Assignment> assignments;
        
        if ((application == null || application.isEmpty()) && (status == null || status.isEmpty())) {
            assignments = assignmentRepository.findBySrid(srid);
        } else {
            assignments = assignmentRepository.findBySridAndFilters(
                    srid,
                    (application != null && !application.isEmpty()) ? application : null,
                    (status != null && !status.isEmpty()) ? status : null
            );
        }
        
        return assignments.stream()
                .map(AssignmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssignmentDto getAssignment(String srid, String assignmentId) {
        Assignment assignment = assignmentRepository.findByAssignmentIdAndSrid(assignmentId, srid)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        
        return AssignmentDto.fromEntity(assignment);
    }

    @Transactional(readOnly = true)
    public List<TaskDto> getAssignmentTasks(String srid, String assignmentId) {
        if (!assignmentRepository.existsByAssignmentIdAndSrid(assignmentId, srid)) {
            throw new ResourceNotFoundException("Assignment not found with id: " + assignmentId);
        }
        
        return taskRepository.findByAssignmentId(assignmentId).stream()
                .map(TaskDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssignmentDto createAssignment(String srid, AssignmentDto assignmentDto) {
        if (assignmentRepository.existsByAssignmentIdAndSrid(assignmentDto.getAssignmentId(), srid)) {
            throw new IllegalArgumentException("Assignment with id " + assignmentDto.getAssignmentId() + " already exists");
        }
        
        Assignment assignment = AssignmentDto.toEntity(assignmentDto);
        assignment.setSrid(srid);
        
        Assignment savedAssignment = assignmentRepository.save(assignment);
        return AssignmentDto.fromEntity(savedAssignment);
    }

    @Transactional
    public AssignmentDto updateAssignment(String srid, String assignmentId, AssignmentDto assignmentDto) {
        Assignment existingAssignment = assignmentRepository.findByAssignmentIdAndSrid(assignmentId, srid)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        
        // Update fields from DTO
        existingAssignment.setApplication(assignmentDto.getApplication());
        existingAssignment.setStream(assignmentDto.getStream());
        existingAssignment.setOwner(assignmentDto.getOwner());
        existingAssignment.setStatus(assignmentDto.getStatus());
        existingAssignment.setReleaseId(assignmentDto.getReleaseId());
        existingAssignment.setSetid(assignmentDto.getSetid());
        existingAssignment.setLevel(assignmentDto.getLevel());
        
        // Update tasks if provided
        if (assignmentDto.getTasks() != null) {
            // Clear existing tasks
            existingAssignment.getTasks().clear();
            
            // Add new tasks
            assignmentDto.getTasks().forEach(taskDto -> {
                Task task = TaskDto.toEntity(taskDto);
                existingAssignment.addTask(task);
            });
        }
        
        Assignment updatedAssignment = assignmentRepository.save(existingAssignment);
        return AssignmentDto.fromEntity(updatedAssignment);
    }

    @Transactional
    public void deleteAssignment(String srid, String assignmentId) {
        Assignment assignment = assignmentRepository.findByAssignmentIdAndSrid(assignmentId, srid)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        
        assignmentRepository.delete(assignment);
    }
}
