package com.codepipeline.mcp.service;

import com.codepipeline.mcp.exception.ResourceNotFoundException;
import com.codepipeline.mcp.model.Message;
import com.codepipeline.mcp.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Message> findAll(Pageable pageable) {
        return messageRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Message findById(String id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));
    }

    @Transactional
    public Message create(Message message) {
        return messageRepository.save(message);
    }

    @Transactional
    public Message update(String id, Message messageDetails) {
        Message message = findById(id);
        message.setContent(messageDetails.getContent());
        message.setSender(messageDetails.getSender());
        return messageRepository.save(message);
    }

    @Transactional
    public void delete(String id) {
        Message message = findById(id);
        messageRepository.delete(message);
    }

    @Transactional(readOnly = true)
    public List<Message> findBySender(String sender) {
        return messageRepository.findBySender(sender);
    }

    @Transactional(readOnly = true)
    public Page<Message> search(String content, Pageable pageable) {
        List<Message> messages = messageRepository.findByContentContainingIgnoreCase(content);
        
        // Apply pagination manually since we're not using JPA's Pageable with custom query
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), messages.size());
        
        return new PageImpl<>(
            messages.subList(start, end),
            pageable,
            messages.size()
        );
    }
}
