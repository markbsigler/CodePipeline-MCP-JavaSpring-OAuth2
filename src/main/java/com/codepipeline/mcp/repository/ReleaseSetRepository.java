package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.model.ReleaseSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseSetRepository extends JpaRepository<ReleaseSet, String> {
    
    Optional<ReleaseSet> findBySetIdAndReleaseId(String setId, String releaseId);
    
    List<ReleaseSet> findByReleaseId(String releaseId);
    
    boolean existsBySetIdAndReleaseId(String setId, String releaseId);
    
    List<ReleaseSet> findByReleaseSrid(String srid);
    
    Optional<ReleaseSet> findBySetIdAndReleaseSrid(String setId, String srid);
}
