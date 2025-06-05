package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MessageRepositoryPostgresIT.TestConfig.class)
public class MessageRepositoryPostgresIT {
    
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("init_postgres.sql")
            .withExposedPorts(5432);

    @Autowired
    private MessageRepository messageRepository;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }
    
    @TestConfiguration
    @EnableTransactionManagement
    public static class TestConfig {
        // Test-specific configuration
    }
    
    @BeforeEach
    void setUp() {
        // Clean up test data before each test
        messageRepository.deleteAll();
    }
    
    @Test
    void contextLoads() {
        assertThat(messageRepository).isNotNull();
    }
    
    @Test
    void whenSaveMessage_thenCanRetrieveIt() {
        // given
        Message message = new Message();
        message.setId("test-message-1");
        message.setContent("Test message content");
        message.setSender("testuser");
        message.setCreatedAt(LocalDateTime.now());
        
        // when
        Message savedMessage = messageRepository.save(message);
        
        // then
        assertThat(savedMessage).isNotNull();
        assertThat(savedMessage.getId()).isEqualTo("test-message-1");
        
        Optional<Message> foundMessage = messageRepository.findById("test-message-1");
        assertThat(foundMessage).isPresent();
        assertThat(foundMessage.get().getContent()).isEqualTo("Test message content");
    }
    
    @Test
    void whenFindById_thenReturnMessage() {
        // given
        Message message = new Message();
        String messageId = "find-by-id-test";
        message.setId(messageId);
        message.setContent("Find by ID test");
        message.setSender("testuser");
        message.setCreatedAt(LocalDateTime.now());
        messageRepository.save(message);
        
        // when
        Optional<Message> foundMessage = messageRepository.findById(messageId);
        
        // then
        assertThat(foundMessage).isPresent();
        assertThat(foundMessage.get().getContent()).isEqualTo("Find by ID test");
    }
    
    @Test
    void whenFindAll_thenReturnAllMessages() {
        // given
        Message message1 = new Message();
        message1.setId("message-1");
        message1.setContent("Message 1");
        message1.setSender("testuser");
        message1.setCreatedAt(LocalDateTime.now());
        messageRepository.save(message1);
        
        Message message2 = new Message();
        message2.setId("message-2");
        message2.setContent("Message 2");
        message2.setSender("testuser");
        message2.setCreatedAt(LocalDateTime.now());
        messageRepository.save(message2);
        
        // when
        List<Message> messages = messageRepository.findAll();
        
        // then
        assertThat(messages).hasSize(2);
        assertThat(messages).extracting(Message::getContent)
                .containsExactlyInAnyOrder("Message 1", "Message 2");
    }
    
    @Test
    void whenDelete_thenMessageIsRemoved() {
        // given
        Message message = new Message();
        String messageId = "delete-test-message";
        message.setId(messageId);
        message.setContent("Delete test message");
        message.setSender("testuser");
        message.setCreatedAt(LocalDateTime.now());
        messageRepository.save(message);
        
        // when
        messageRepository.deleteById(messageId);
        
        // then
        assertThat(messageRepository.findById(messageId)).isNotPresent();
    }
    
    @Test
    void whenUpdate_thenMessageIsUpdated() {
        // given
        Message message = new Message();
        String messageId = "update-test-message";
        message.setId(messageId);
        message.setContent("Original content");
        message.setSender("testuser");
        message.setCreatedAt(LocalDateTime.now());
        messageRepository.save(message);
        
        // when
        Message foundMessage = messageRepository.findById(messageId).orElseThrow();
        foundMessage.setContent("Updated content");
        messageRepository.save(foundMessage);
        
        // then
        Message updatedMessage = messageRepository.findById(messageId).orElseThrow();
        assertThat(updatedMessage.getContent()).isEqualTo("Updated content");
    }
    
    @Test
    void whenFindBySender_thenReturnMessages() {
        // given
        Message message1 = new Message();
        message1.setId("sender-msg-1");
        message1.setContent("Message from testuser");
        message1.setSender("testuser");
        message1.setCreatedAt(LocalDateTime.now());
        messageRepository.save(message1);
        
        Message message2 = new Message();
        message2.setId("sender-msg-2");
        message2.setContent("Another message from testuser");
        message2.setSender("testuser");
        message2.setCreatedAt(LocalDateTime.now());
        messageRepository.save(message2);
        
        // Add a message from a different sender
        Message otherMessage = new Message();
        otherMessage.setId("other-sender-msg");
        otherMessage.setContent("Message from otheruser");
        otherMessage.setSender("otheruser");
        otherMessage.setCreatedAt(LocalDateTime.now());
        messageRepository.save(otherMessage);
        
        // when
        List<Message> messages = messageRepository.findBySender("testuser");
        
        // then
        assertThat(messages).hasSize(2);
        assertThat(messages).extracting(Message::getSender)
                .allMatch(sender -> sender.equals("testuser"));
        assertThat(messages).extracting(Message::getContent)
                .containsExactlyInAnyOrder(
                    "Message from testuser",
                    "Another message from testuser"
                );
    }
}
