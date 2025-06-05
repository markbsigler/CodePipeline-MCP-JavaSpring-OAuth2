package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    
    Optional<Task> findByTaskIdAndAssignmentId(String taskId, String assignmentId);
    
    List<Task> findByAssignmentId(String assignmentId);
    
    boolean existsByTaskIdAndAssignmentId(String taskId, String assignmentId);
}
