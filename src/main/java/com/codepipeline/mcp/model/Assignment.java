package com.codepipeline.mcp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true)
    private String assignmentId;
    
    @Column(nullable = false)
    private String srid;
    
    private String application;
    private String stream;
    private String owner;
    private String status;
    private String releaseId;
    private String setid;
    private String level;
    
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public void addTask(Task task) {
        tasks.add(task);
        task.setAssignment(this);
    }
    
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setAssignment(null);
    }
}
