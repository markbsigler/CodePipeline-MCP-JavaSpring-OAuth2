package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.model.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseRepository extends JpaRepository<Release, String> {
    
    Optional<Release> findByReleaseIdAndSrid(String releaseId, String srid);
    
    List<Release> findBySrid(String srid);
    
    @Query("SELECT r FROM Release r WHERE r.srid = :srid AND " +
           "(:application IS NULL OR r.application = :application) AND " +
           "(:status IS NULL OR r.status = :status)")
    List<Release> findBySridAndFilters(
            @Param("srid") String srid,
            @Param("application") String application,
            @Param("status") String status
    );
    
    boolean existsByReleaseIdAndSrid(String releaseId, String srid);
}
