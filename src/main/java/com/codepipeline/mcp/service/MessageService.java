package com.codepipeline.mcp.service;

import com.codepipeline.mcp.exception.ResourceNotFoundException;
import com.codepipeline.mcp.model.Message;
import com.codepipeline.mcp.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing messages.
 * Provides transactional operations for creating, reading, updating, and deleting messages,
 * as well as searching and filtering capabilities.
 * 
 * <p>All write operations are transactional and will be rolled back in case of errors.
 * Read operations are marked as read-only to optimize database access.</p>
 * 
 * <p>Input validation is performed for all operations that modify data to ensure data integrity.</p>
 */

@Service
@RequiredArgsConstructor
public class MessageService {
    
    /**
     * The message repository for database operations.
     */
    private final MessageRepository messageRepository;


    /**
     * Retrieves all messages from the database.
     *
     * @return a list of all messages
     */
    @Transactional(readOnly = true)
    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    /**
     * Retrieves a page of messages with pagination support.
     *
     * @param pageable pagination information
     * @return a page of messages
     */
    @Transactional(readOnly = true)
    public Page<Message> findAll(Pageable pageable) {
        return messageRepository.findAll(pageable);
    }

    /**
     * Finds a message by its ID.
     *
     * @param id the message ID to search for
     * @return the found message
     * @throws ResourceNotFoundException if no message is found with the given ID
     */
    @Transactional(readOnly = true)
    public Message findById(String id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));
    }

    /**
     * Creates a new message.
     *
     * @param message the message to create
     * @return the created message with generated ID and timestamps
     * @throws IllegalArgumentException if the message is null, or if content or sender is null or empty
     */
    @Transactional
    public Message create(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }
        if (message.getSender() == null || message.getSender().trim().isEmpty()) {
            throw new IllegalArgumentException("Message sender cannot be null or empty");
        }
        return messageRepository.save(message);
    }

    /**
     * Updates an existing message.
     *
     * @param id the ID of the message to update
     * @param messageDetails the message details to update
     * @return the updated message
     * @throws ResourceNotFoundException if no message is found with the given ID
     * @throws IllegalArgumentException if messageDetails is null
     */
    @Transactional
    public Message update(String id, Message messageDetails) {
        Message message = findById(id);
        message.setContent(messageDetails.getContent());
        message.setSender(messageDetails.getSender());
        return messageRepository.save(message);
    }

    /**
     * Deletes a message by its ID.
     *
     * @param id the ID of the message to delete
     * @throws ResourceNotFoundException if no message is found with the given ID
     */
    @Transactional
    public void delete(String id) {
        Message message = findById(id);
        messageRepository.delete(message);
    }

    /**
     * Finds all messages sent by a specific sender.
     *
     * @param sender the sender's username
     * @return a list of messages from the specified sender
     */
    @Transactional(readOnly = true)
    public List<Message> findBySender(String sender) {
        return messageRepository.findBySender(sender);
    }
    
    /**
     * Finds a page of messages sent by a specific sender with pagination support.
     *
     * @param sender the sender's username
     * @param pageable pagination information
     * @return a page of messages from the specified sender
     */
    @Transactional(readOnly = true)
    public Page<Message> findBySender(String sender, Pageable pageable) {
        return messageRepository.findBySender(sender, pageable);
    }

    /**
     * Searches for messages containing the given query string in their content (case-insensitive).
     *
     * @param query the search term
     * @param pageable pagination information
     * @return a page of matching messages
     */
    @Transactional(readOnly = true)
    public Page<Message> search(String query, Pageable pageable) {
        // Use the paginated case-insensitive search method from the repository
        return messageRepository.findByContentContainingIgnoreCase(query, pageable);
    }
}
