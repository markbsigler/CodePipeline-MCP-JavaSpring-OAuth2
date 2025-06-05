package com.codepipeline.mcp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "releases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Release {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true)
    private String releaseId;
    
    @Column(nullable = false)
    private String srid;
    
    private String application;
    private String stream;
    private String owner;
    private String status;
    private String description;
    
    @OneToMany(mappedBy = "release", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ReleaseSet> sets = new HashSet<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public void addSet(ReleaseSet releaseSet) {
        sets.add(releaseSet);
        releaseSet.setRelease(this);
    }
    
    public void removeSet(ReleaseSet releaseSet) {
        sets.remove(releaseSet);
        releaseSet.setRelease(null);
    }
}
