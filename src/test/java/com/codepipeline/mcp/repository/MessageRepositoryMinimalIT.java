package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.model.Message;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:test-application.properties")
class MessageRepositoryMinimalIT {
    @BeforeAll
    static void checkDockerAvailability() {
        boolean dockerAvailable = false;
        try {
            Process process = new ProcessBuilder("docker", "info").start();
            int exitCode = process.waitFor();
            dockerAvailable = (exitCode == 0);
        } catch (Exception e) {
            dockerAvailable = false;
        }
        Assumptions.assumeTrue(dockerAvailable, "Docker is not available. Skipping Docker-dependent integration tests.");
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MessageRepository messageRepository;

    private Message testMessage;

    @BeforeEach
    void setUp() {
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setContent("Test message");
        message.setSender("test@example.com");
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        message.setVersion(0L);
        
        testMessage = entityManager.merge(message);
        entityManager.flush();
    }

    @Test
    void whenFindById_thenReturnMessage() {
        // when
        Optional<Message> found = messageRepository.findById(testMessage.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo(testMessage.getContent());
    }

    @Test
    void whenFindAll_thenReturnAllMessages() {
        // given
        Message anotherMessage = new Message();
        anotherMessage.setId(UUID.randomUUID().toString());
        anotherMessage.setContent("Another test message");
        anotherMessage.setSender("another@example.com");
        anotherMessage.setCreatedAt(LocalDateTime.now());
        anotherMessage.setUpdatedAt(LocalDateTime.now());
        anotherMessage.setVersion(0L);
        
        anotherMessage = entityManager.merge(anotherMessage);
        entityManager.flush();

        // when
        List<Message> messages = messageRepository.findAll();

        // then
        assertThat(messages).hasSize(2);
        assertThat(messages).extracting(Message::getContent)
                .contains(testMessage.getContent(), anotherMessage.getContent());
    }

    @Test
    void whenSave_thenMessageIsSaved() {
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
    void whenDelete_thenMessageIsRemoved() {
        // when
        messageRepository.delete(testMessage);
        entityManager.flush();

        // then
        assertThat(messageRepository.findById(testMessage.getId())).isEmpty();
    }
}
