package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String> {
    
    Optional<Assignment> findByAssignmentIdAndSrid(String assignmentId, String srid);
    
    List<Assignment> findBySrid(String srid);
    
    @Query("SELECT a FROM Assignment a WHERE a.srid = :srid AND " +
           "(:application IS NULL OR a.application = :application) AND " +
           "(:status IS NULL OR a.status = :status)")
    List<Assignment> findBySridAndFilters(
            @Param("srid") String srid,
            @Param("application") String application,
            @Param("status") String status
    );
    
    boolean existsByAssignmentIdAndSrid(String assignmentId, String srid);
}
