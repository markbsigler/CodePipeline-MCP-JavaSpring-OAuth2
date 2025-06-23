package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String>, JpaSpecificationExecutor<Message> {
    
    /**
     * Find all messages sent by a specific sender
     * @param sender the sender's username
     * @return list of messages sent by the specified sender
     */
    List<Message> findBySender(String sender);
    
    @Query("SELECT m FROM Message m WHERE LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Message> findByContentContainingIgnoreCase(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT m FROM Message m WHERE LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Message> findByContentContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    long countBySender(String sender);
}
