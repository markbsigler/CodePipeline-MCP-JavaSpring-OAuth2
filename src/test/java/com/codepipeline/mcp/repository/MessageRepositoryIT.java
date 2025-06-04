package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.BaseIntegrationTest;
import com.codepipeline.mcp.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:test-application.yml")
@Transactional
@DisplayName("Message Repository Integration Tests")
class MessageRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private MessageRepository messageRepository;
    
    private static final String TEST_SENDER = "testuser@example.com";

    private Message createTestMessage(String content, String sender) {
        return Message.builder()
                .content(content)
                .sender(sender)
                .build();
    }
    
    private List<Message> createTestMessages(int count, String contentPrefix, String sender) {
        return IntStream.range(0, count)
                .mapToObj(i -> createTestMessage(contentPrefix + " " + (i + 1), sender))
                .collect(Collectors.toList());
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {
        private Message testMessage;

        @BeforeEach
        void setUp() {
            messageRepository.deleteAll();
            testMessage = createTestMessage("Test message", TEST_SENDER);
        }

        @Test
        @DisplayName("should save message with generated ID and timestamps")
        void shouldSaveMessage() {
            // When
            Message savedMessage = messageRepository.save(testMessage);

            // Then
            assertThat(savedMessage).isNotNull();
            assertThat(savedMessage.getId())
                    .isNotBlank()
                    .satisfies(id -> assertThat(UUID.fromString(id)).isNotNull());
            assertThat(savedMessage.getContent()).isEqualTo("Test message");
            assertThat(savedMessage.getSender()).isEqualTo(TEST_SENDER);
            assertThat(savedMessage.getCreatedAt())
                    .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(savedMessage.getUpdatedAt())
                    .isCloseTo(savedMessage.getCreatedAt(), within(1, ChronoUnit.MILLIS));
            assertThat(savedMessage.getVersion()).isZero();
            
            // Verify in database
            Optional<Message> foundMessage = messageRepository.findById(savedMessage.getId());
            assertThat(foundMessage).isPresent();
            assertThat(foundMessage.get()).usingRecursiveComparison().isEqualTo(savedMessage);
        }

        @Test
        @DisplayName("should find message by ID")
        void shouldFindMessageById() {
            // Given
            Message savedMessage = messageRepository.save(testMessage);

            // When
            Optional<Message> foundMessage = messageRepository.findById(savedMessage.getId());

            // Then
            assertThat(foundMessage)
                    .isPresent()
                    .get()
                    .usingRecursiveComparison()
                    .isEqualTo(savedMessage);
        }
        
        @Test
        @DisplayName("should return empty when message not found by ID")
        void shouldReturnEmptyWhenMessageNotFound() {
            // When
            Optional<Message> foundMessage = messageRepository.findById("nonexistent-id");
            
            // Then
            assertThat(foundMessage).isNotPresent();
        }

        @Test
        @DisplayName("should return all messages")
        void shouldFindAllMessages() {
            // Given
            List<Message> testMessages = List.of(
                createTestMessage("First message", "user1@example.com"),
                createTestMessage("Second message", "user2@example.com")
            );
            messageRepository.saveAll(testMessages);

            // When
            List<Message> messages = messageRepository.findAll();

            // Then
            assertThat(messages)
                    .hasSize(2)
                    .extracting(Message::getContent)
                    .containsExactlyInAnyOrder("First message", "Second message");
        }
        
        @Test
        @DisplayName("should return empty list when no messages exist")
        void shouldReturnEmptyListWhenNoMessages() {
            // Given - no messages in database
            
            // When
            List<Message> messages = messageRepository.findAll();
            
            // Then
            assertThat(messages).isEmpty();
        }

        @Test
        @DisplayName("should update message content and update timestamp")
        void shouldUpdateMessage() {
            // Given
            Message savedMessage = messageRepository.save(testMessage);
            LocalDateTime originalCreatedAt = savedMessage.getCreatedAt();
            LocalDateTime originalUpdatedAt = savedMessage.getUpdatedAt();
            
            // Small delay to ensure timestamps differ
            try { Thread.sleep(10); } catch (InterruptedException e) {}

            // When
            savedMessage.setContent("Updated content");
            Message updatedMessage = messageRepository.saveAndFlush(savedMessage);

            // Then
            assertThat(updatedMessage.getContent()).isEqualTo("Updated content");
            assertThat(updatedMessage.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(updatedMessage.getUpdatedAt())
                    .isAfter(originalUpdatedAt)
                    .isAfter(updatedMessage.getCreatedAt());
                    
            // Verify in database
            Message dbMessage = messageRepository.findById(savedMessage.getId()).orElseThrow();
            assertThat(dbMessage.getContent()).isEqualTo("Updated content");
            assertThat(dbMessage.getUpdatedAt()).isAfter(originalUpdatedAt);
        }
        
        @Test
        @DisplayName("should increment version on update")
        void shouldIncrementVersionOnUpdate() {
            // Given
            Message savedMessage = messageRepository.save(testMessage);
            Long originalVersion = savedMessage.getVersion();
            
            // When - first update
            savedMessage.setContent("First update");
            Message firstUpdate = messageRepository.saveAndFlush(savedMessage);
            
            // Then
            assertThat(firstUpdate.getVersion()).isEqualTo(originalVersion + 1);
            
            // When - second update
            firstUpdate.setContent("Second update");
            Message secondUpdate = messageRepository.saveAndFlush(firstUpdate);
            
            // Then
            assertThat(secondUpdate.getVersion()).isEqualTo(originalVersion + 2);
        }

        @Test
        @DisplayName("should delete message")
        void shouldDeleteMessage() {
            // Given
            Message savedMessage = messageRepository.save(testMessage);
            assertThat(messageRepository.count()).isEqualTo(1);

            // When
            messageRepository.delete(savedMessage);
            
            // Then
            assertThat(messageRepository.count()).isZero();
            assertThat(messageRepository.findById(savedMessage.getId())).isNotPresent();
        }
        
        @Test
        @DisplayName("should delete all messages")
        void shouldDeleteAllMessages() {
            // Given
            messageRepository.saveAll(List.of(
                createTestMessage("Message 1", "user1@example.com"),
                createTestMessage("Message 2", "user2@example.com")
            ));
            assertThat(messageRepository.count()).isEqualTo(2);
            
            // When
            messageRepository.deleteAll();
            
            // Then
            assertThat(messageRepository.count()).isZero();
        }
    }


    @Nested
    @DisplayName("Query Methods")
    class QueryMethods {
        private static final String ALICE_EMAIL = "alice@example.com";
        private static final String BOB_EMAIL = "bob@example.com";
        
        @BeforeEach
        void setUp() {
            messageRepository.saveAll(List.of(
                createTestMessage("Hello World", ALICE_EMAIL),
                createTestMessage("Spring Boot is awesome", BOB_EMAIL),
                createTestMessage("Testing is important", ALICE_EMAIL)
            ));
        }

        @Test
        @DisplayName("should find messages by sender")
        void shouldFindBySender() {
            // When
            List<Message> aliceMessages = messageRepository.findBySender(ALICE_EMAIL);

            // Then
            assertThat(aliceMessages)
                    .hasSize(2)
                    .allMatch(msg -> ALICE_EMAIL.equals(msg.getSender()));
        }
        
        @Test
        @DisplayName("should return empty list when no messages from sender")
        void shouldReturnEmptyListWhenNoMessagesFromSender() {
            // When
            List<Message> messages = messageRepository.findBySender("nonexistent@example.com");
            
            // Then
            assertThat(messages).isEmpty();
        }
        
        @Test
        @DisplayName("Should find messages by content containing search term (case insensitive)")
        void shouldFindMessagesByContentContainingSearchTermCaseInsensitive() {
            // Given
            messageRepository.save(createTestMessage("Spring Boot is awesome", "test@example.com"));
            messageRepository.save(createTestMessage("Another message", "test2@example.com"));
            
            // When - Use the case-insensitive search method with pagination
            Pageable pageable = PageRequest.of(0, 10);
            Page<Message> messagePage = messageRepository.findByContentContainingIgnoreCase("sPrInG", pageable);
            List<Message> messages = messagePage.getContent();
            
            // Then - Should find the message regardless of case
            assertThat(messages)
                    .hasSize(1)
                    .extracting(Message::getContent)
                    .containsExactly("Spring Boot is awesome");
                    
            // Verify pagination info
            assertThat(messagePage.getTotalElements()).isEqualTo(1);
            assertThat(messagePage.getTotalPages()).isEqualTo(1);
            assertThat(messagePage.getNumber()).isEqualTo(0);
        }
        
        @Test
        @DisplayName("should find all with pagination")
        void shouldFindAllWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 2, Sort.by("content"));
            
            // When
            Page<Message> page = messageRepository.findAll(pageable);
            
            // Then
            assertThat(page.getContent())
                    .hasSize(2)
                    .extracting(Message::getContent)
                    .containsExactly("Hello World", "Spring Boot is awesome");
            
            assertThat(page.getTotalElements()).isEqualTo(3);
            assertThat(page.getTotalPages()).isEqualTo(2);
        }
        
        @Test
        @DisplayName("should find all with custom sort")
        void shouldFindAllWithCustomSort() {
            // Given
            Sort sort = Sort.by(Sort.Direction.DESC, "content");
            
            // When
            List<Message> messages = messageRepository.findAll(sort);
            
            // Then
            assertThat(messages)
                    .hasSize(3)
                    .extracting(Message::getContent)
                    .containsExactly("Testing is important", "Spring Boot is awesome", "Hello World");
        }
    }
    
    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        private static final int LARGE_DATASET_SIZE = 1000;
        
        @BeforeEach
        void setUp() {
            // Create a large dataset
            List<Message> messages = createTestMessages(LARGE_DATASET_SIZE, "Test Message", "perf@example.com");
            messageRepository.saveAll(messages);
        }
        
        @Test
        @DisplayName("should efficiently retrieve paginated results from large dataset")
        void shouldEfficientlyRetrievePaginatedResults() {
            // Given
            int pageSize = 20;
            int totalPages = (int) Math.ceil((double) LARGE_DATASET_SIZE / pageSize);
            
            // When / Then - verify we can paginate through all results
            for (int page = 0; page < Math.min(5, totalPages); page++) {
                Pageable pageable = PageRequest.of(page, pageSize);
                Page<Message> result = messageRepository.findAll(pageable);
                
                assertThat(result.getContent()).hasSize(pageSize);
                assertThat(result.getNumber()).isEqualTo(page);
                assertThat(result.getTotalElements()).isEqualTo(LARGE_DATASET_SIZE);
                assertThat(result.getTotalPages()).isEqualTo(totalPages);
            }
        }
    }
}
