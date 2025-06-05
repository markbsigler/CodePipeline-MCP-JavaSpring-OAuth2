package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:test-application.properties")
public class MessageRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MessageRepository messageRepository;

    private Message testMessage;

    @BeforeEach
    public void setUp() {
        // Create a new message for testing
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setContent("Test message");
        message.setSender("test@example.com");
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        message.setVersion(0L);
        
        // Persist the test message
        testMessage = entityManager.merge(message);
        entityManager.flush();
    }

    @Test
    public void whenFindById_thenReturnMessage() {
        // when
        Optional<Message> found = messageRepository.findById(testMessage.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo(testMessage.getContent());
    }

    @Test
    public void whenFindAll_thenReturnAllMessages() {
        // given
        Message anotherMessage = new Message();
        anotherMessage.setId(UUID.randomUUID().toString());
        anotherMessage.setContent("Another test message");
        anotherMessage.setSender("another@example.com");
        anotherMessage.setCreatedAt(LocalDateTime.now());
        anotherMessage.setUpdatedAt(LocalDateTime.now());
        anotherMessage.setVersion(0L);
        
        entityManager.merge(anotherMessage);
        entityManager.flush();

        // when
        List<Message> messages = messageRepository.findAll();

        // then
        assertThat(messages).hasSize(2);
        assertThat(messages).extracting(Message::getContent)
                .contains(testMessage.getContent(), anotherMessage.getContent());
    }

    @Test
    public void whenSave_thenMessageIsSaved() {
        // given
        Message newMessage = new Message();
        newMessage.setId(UUID.randomUUID().toString());
        newMessage.setContent("New test message");
        newMessage.setSender("new@example.com");
        newMessage.setCreatedAt(LocalDateTime.now());
        newMessage.setUpdatedAt(LocalDateTime.now());
        newMessage.setVersion(0L);

        // when
        Message saved = messageRepository.save(newMessage);
        entityManager.flush();

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(messageRepository.findById(saved.getId())).isPresent();
    }

    @Test
    public void whenDelete_thenMessageIsRemoved() {
        // when
        messageRepository.delete(testMessage);
        entityManager.flush();

        // then
        assertThat(messageRepository.findById(testMessage.getId())).isEmpty();
    }
}
