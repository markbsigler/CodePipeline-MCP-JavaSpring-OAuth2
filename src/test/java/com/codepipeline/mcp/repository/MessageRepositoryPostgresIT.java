package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
public class MessageRepositoryPostgresIT extends BasePostgresRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    private Message testMessage;

    @BeforeEach
    void setUp() {
        // Create a test message
        testMessage = new Message();
        testMessage.setId(UUID.randomUUID().toString());
        testMessage.setContent("Test message content");
        testMessage.setSender("testuser");
        testMessage.setCreatedAt(Instant.now());
        testMessage.setVersion(0L);
        
        // Clear any existing data
        messageRepository.deleteAll();
    }

    @Test
    void shouldSaveMessage() {
        // When
        Message savedMessage = messageRepository.save(testMessage);
        
        // Then
        assertThat(savedMessage).isNotNull();
        assertThat(savedMessage.getId()).isEqualTo(testMessage.getId());
        assertThat(savedMessage.getContent()).isEqualTo(testMessage.getContent());
        assertThat(savedMessage.getSender()).isEqualTo(testMessage.getSender());
        assertThat(savedMessage.getCreatedAt()).isNotNull();
        assertThat(savedMessage.getVersion()).isEqualTo(0L);
    }

    @Test
    void shouldFindMessageById() {
        // Given
        Message savedMessage = messageRepository.save(testMessage);
        
        // When
        Optional<Message> foundMessage = messageRepository.findById(savedMessage.getId());
        
        // Then
        assertThat(foundMessage).isPresent();
        assertThat(foundMessage.get()).usingRecursiveComparison().isEqualTo(savedMessage);
    }

    @Test
    void shouldFindAllMessages() {
        // Given
        messageRepository.save(testMessage);
        
        // Create a second message
        Message anotherMessage = new Message();
        anotherMessage.setId(UUID.randomUUID().toString());
        anotherMessage.setContent("Another test message");
        anotherMessage.setSender("anotheruser");
        anotherMessage.setCreatedAt(Instant.now());
        anotherMessage.setVersion(0L);
        messageRepository.save(anotherMessage);
        
        // When
        List<Message> messages = messageRepository.findAll();
        
        // Then
        assertThat(messages).hasSize(2);
        assertThat(messages).extracting(Message::getId)
                .containsExactlyInAnyOrder(testMessage.getId(), anotherMessage.getId());
    }

    @Test
    void shouldUpdateMessage() {
        // Given
        Message savedMessage = messageRepository.save(testMessage);
        
        // When
        savedMessage.setContent("Updated content");
        savedMessage.setSender("updateduser");
        Message updatedMessage = messageRepository.save(savedMessage);
        
        // Then
        assertThat(updatedMessage.getContent()).isEqualTo("Updated content");
        assertThat(updatedMessage.getSender()).isEqualTo("updateduser");
        assertThat(updatedMessage.getVersion()).isEqualTo(1L); // Version should be incremented
    }

    @Test
    void shouldDeleteMessage() {
        // Given
        Message savedMessage = messageRepository.save(testMessage);
        
        // When
        messageRepository.deleteById(savedMessage.getId());
        
        // Then
        assertThat(messageRepository.findById(savedMessage.getId())).isEmpty();
    }

    @Test
    void shouldFindBySender() {
        // Given
        messageRepository.save(testMessage);
        
        // Create another message from the same sender
        Message anotherMessage = new Message();
        anotherMessage.setId(UUID.randomUUID().toString());
        anotherMessage.setContent("Another test message");
        anotherMessage.setSender("testuser");
        anotherMessage.setCreatedAt(Instant.now());
        anotherMessage.setVersion(0L);
        messageRepository.save(anotherMessage);
        
        // When
        List<Message> messages = messageRepository.findBySender("testuser");
        
        // Then
        assertThat(messages).hasSize(2);
        assertThat(messages).extracting(Message::getSender).containsOnly("testuser");
    }
}
