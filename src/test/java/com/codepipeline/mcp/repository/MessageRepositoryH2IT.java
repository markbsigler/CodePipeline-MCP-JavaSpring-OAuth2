package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.config.RepositoryTestConfig;
import com.codepipeline.mcp.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(RepositoryTestConfig.class)
public class MessageRepositoryH2IT {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MessageRepository messageRepository;

    private Message testMessage;

    @BeforeEach
    void setUp() {
        testMessage = new Message();
        testMessage.setId(UUID.randomUUID().toString());
        testMessage.setContent("Test message");
        testMessage.setSender("test@example.com");
        testMessage.setCreatedAt(LocalDateTime.now());
        testMessage.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(testMessage);
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
        entityManager.persistAndFlush(anotherMessage);

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

        // when
        Message saved = messageRepository.save(newMessage);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(messageRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void whenDelete_thenMessageIsRemoved() {
        // when
        messageRepository.delete(testMessage);

        // then
        assertThat(messageRepository.findById(testMessage.getId())).isEmpty();
    }
}
