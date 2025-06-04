package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findBySender(String sender);
    
    List<Message> findByContentContainingIgnoreCase(String content);
    
    long countBySender(String sender);
}
