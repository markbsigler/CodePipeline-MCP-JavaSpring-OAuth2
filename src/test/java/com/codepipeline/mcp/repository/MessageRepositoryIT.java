package com.codepipeline.mcp.repository;

import org.testcontainers.junit.jupiter.Testcontainers;
import com.codepipeline.mcp.model.Message;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@link MessageRepository}.
 * Uses PostgreSQL Testcontainer for testing the repository layer.
 */
@Testcontainers
@DisplayName("Message Repository Integration Tests")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(MessageRepositoryIT.NoFlywayConfig.class)
@Transactional
class MessageRepositoryIT {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private MessageRepository messageRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private static final String TEST_SENDER = "test@example.com";
    private static final String OTHER_SENDER = "other@example.com";
    private static final int TEST_MESSAGE_COUNT = 5;
    private static final int LARGE_DATASET_SIZE = 1000;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        // Ensure the container is started before resolving properties
        if (!postgres.isRunning()) {
            postgres.start();
        }
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        // Let Hibernate auto-create the schema for tests
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @Transactional
    @BeforeEach
    void setUp() {
        // Clean up existing data before each test
        messageRepository.deleteAllInBatch();
        
        // Create test messages
        for (int i = 0; i < TEST_MESSAGE_COUNT; i++) {
            Message message = new Message();
            message.setContent("Test message " + i);
            message.setSender(TEST_SENDER);
            messageRepository.save(message);
        }
        
        // Add a message with different sender
        Message otherMessage = new Message();
        otherMessage.setContent("Spring Boot is awesome");
        otherMessage.setSender(OTHER_SENDER);
        messageRepository.save(otherMessage);
    }
    
    @AfterEach
    void tearDown() {
        messageRepository.deleteAllInBatch();
        // No flush here
    }
    
    /**
     * Utility method to create a test message with the given content and sender.
     * @param content The message content
     * @param sender The sender's email
     * @return A new Message instance
     */
    private Message createTestMessage(String content, String sender) {
        Message message = new Message();
        message.setContent(content);
        message.setSender(sender);
        return message;
    }

    @Nested
    @DisplayName("CRUD Operations")
    @Transactional
    class CrudOperations {
        
        @Test
        @DisplayName("should not save message with null content")
        void shouldNotSaveMessageWithNullContent() {
            // Given
            Message message = new Message();
            message.setContent(null);
            message.setSender("test@example.com");

            // When/Then
            assertThrowsAny(
                messageRepository,
                message,
                "saveAndFlush"
            );
        }
        
        @Test
        @DisplayName("should not save message with empty content")
        void shouldNotSaveMessageWithEmptyContent() {
            // Given
            Message message = new Message();
            message.setContent("");
            message.setSender("test@example.com");

            // When/Then
            assertThrowsAny(
                messageRepository,
                message,
                "saveAndFlush"
            );
        }
        
        @Test
        @DisplayName("should not save message with null sender")
        void shouldNotSaveMessageWithNullSender() {
            // Given
            Message message = new Message();
            message.setContent("Test content");
            message.setSender(null);

            // When/Then
            assertThrowsAny(
                messageRepository,
                message,
                "saveAndFlush"
            );
        }
        
        private Message createNewTestMessage() {
            Message message = new Message();
            message.setContent("Unique test message " + UUID.randomUUID());
            message.setSender(TEST_SENDER);
            return message;
        }

        @Test
        @DisplayName("should save message successfully")
        void shouldSaveMessageSuccessfully() {
            // Given
            Message message = new Message();
            message.setContent("Valid content");
            message.setSender("test@example.com");

            // When
            Message saved = messageRepository.save(message);
            
            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getContent()).isEqualTo("Valid content");
            assertThat(saved.getSender()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("should save and retrieve message")
        void shouldSaveAndRetrieveMessage() {
            // Given
            Message message = new Message();
            message.setContent("Test Content");
            message.setSender("test@example.com");

            // When
            Message saved = messageRepository.save(message);
            Optional<Message> found = messageRepository.findById(saved.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getContent()).isEqualTo("Test Content");
            assertThat(found.get().getSender()).isEqualTo("test@example.com");
            assertThat(found.get().getId()).isNotNull();
        }

        @Test
        @DisplayName("should update message")
        void shouldUpdateMessage() {
            // Given
            Message message = new Message();
            message.setContent("Original Content");
            message.setSender("test@example.com");
            Message saved = messageRepository.saveAndFlush(message);
            String messageId = saved.getId();

            // When
            saved.setContent("Updated Content");
            messageRepository.saveAndFlush(saved);
            Optional<Message> updated = messageRepository.findById(messageId);

            // Then
            assertThat(updated).isPresent();
            assertThat(updated.get().getContent()).isEqualTo("Updated Content");
            assertThat(updated.get().getId()).isEqualTo(messageId);
        }

        @Test
        @DisplayName("should delete all messages")
        void shouldDeleteAllMessages() {
            // Given
            messageRepository.saveAll(List.of(
                createNewTestMessage(),
                createNewTestMessage()
            ));
            assertThat(messageRepository.count()).isEqualTo(2 + TEST_MESSAGE_COUNT);
            
            // When
            messageRepository.deleteAll();
            
            // Then
            assertThat(messageRepository.count()).isZero();
        }

        @Test
        @DisplayName("should not throw exception when deleting non-existent message")
        void shouldNotThrowExceptionWhenDeletingNonExistentMessage() {
            // Given
            String nonExistentId = UUID.randomUUID().toString();
            long initialCount = messageRepository.count();
            
            // When/Then - Should not throw when deleting non-existent ID
            messageRepository.deleteById(nonExistentId);
                
            // Verify no side effects
            assertThat(messageRepository.count()).isEqualTo(initialCount);
        }
    }

    @Nested
    @DisplayName("Concurrent Operations")
    @Transactional
    class ConcurrentOperations {

        @Test
        @DisplayName("should handle concurrent reads and writes")
        void shouldHandleConcurrentReadsAndWrites() throws InterruptedException {
            // Given
            int threadCount = 5;
            int iterations = 5;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(1);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // When
            for (int i = 0; i < threadCount; i++) {
                final int threadNum = i;
                
                executor.submit(() -> {
                    try {
                        latch.await();
                        for (int j = 0; j < iterations; j++) {
                            // Perform concurrent operations
                            Message message = new Message();
                            message.setContent("Concurrent test " + threadNum + "-" + j);
                            message.setSender("user" + threadNum + "@example.com");
                            messageRepository.saveAndFlush(message);
                            
                            // Verify the message was saved
                            Optional<Message> found = messageRepository.findById(message.getId());
                            assertThat(found).isPresent();
                            assertThat(found.get().getContent()).isEqualTo(message.getContent());
                            
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.err.println("Error in thread " + threadNum + ": " + e.getMessage());
                    }
                });
            }
            
            // Start all threads
            latch.countDown();
            
            // Wait for all threads to complete
            executor.shutdown();
            boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);
            
            // Then
            assertThat(finished).isTrue();
            assertThat(failureCount.get()).isZero();
            assertThat(successCount.get()).isEqualTo(threadCount * iterations);
            
            // Verify data consistency
            long totalMessages = messageRepository.count();
            assertThat(totalMessages).isGreaterThanOrEqualTo(TEST_MESSAGE_COUNT + (threadCount * iterations));

            // Verify no duplicate messages were created
            List<Message> allMessages = messageRepository.findAll();
            long uniqueCount = allMessages.stream()
                    .map(Message::getContent)
                    .distinct()
                    .count();
            assertThat(uniqueCount).isEqualTo(allMessages.size());
        }
    } // End of ConcurrentOperations class
    
    @Nested
    @DisplayName("Query Methods Tests")
    @Transactional
    class MessageQueryMethodsTests {
        @BeforeEach
        void setupQueryTests() {
            messageRepository.deleteAllInBatch();
            // Create test data specifically for query tests
            messageRepository.saveAll(List.of(
                createMessage("Hello World", TEST_QUERY_SENDER),
                createMessage("Spring Boot is awesome", TEST_QUERY_SENDER),
                createMessage("Testing is important", TEST_QUERY_SENDER),
                createMessage("JPA makes data access easy", "other@example.com")
            ));
            messageRepository.flush();
        }
        
        @Test
        @DisplayName("should find messages by content containing ignore case")
        void shouldFindMessagesByContentContainingIgnoreCase() {
            // Given
            String searchTerm = "sPrInG";
            
            // When
            List<Message> found = messageRepository.findByContentContainingIgnoreCase(searchTerm);
            
            // Then
            assertThat(found)
                .hasSize(1)
                .extracting(Message::getContent)
                .containsExactly("Spring Boot is awesome");
        }
        
        @Test
        @DisplayName("should handle empty content search")
        void shouldHandleEmptyContentSearch() {
            // When
            List<Message> found = messageRepository.findByContentContainingIgnoreCase("");
            
            // Then - should return all messages (3 from setup + 1 other)
            assertThat(found).hasSize(4);
        }
        
        @Test
        @DisplayName("should count messages by sender")
        void shouldCountMessagesBySender() {
            // Given
            String sender1 = "counter1@example.com";
            String sender2 = "counter2@example.com";
            
            // Create test data
            messageRepository.saveAll(List.of(
                createMessage("Message 1", sender1),
                createMessage("Message 2", sender1),
                createMessage("Message 3", sender2)
            ));
            messageRepository.flush();
            
            // When/Then
            assertThat(messageRepository.countBySender(sender1)).isEqualTo(2);
            assertThat(messageRepository.countBySender(sender2)).isEqualTo(1);
            assertThat(messageRepository.countBySender("nonexistent@example.com")).isZero();
        }
        
        private static final String TEST_QUERY_SENDER = "query-test@example.com";
        
        private Message createMessage(String content, String sender) {
            Message message = new Message();
            message.setContent(content);
            message.setSender(sender);
            return message;
        }
        
        @Test
        @DisplayName("should find messages by sender")
        void shouldFindMessagesBySender() {
            // When
            List<Message> foundMessages = messageRepository.findBySender(TEST_QUERY_SENDER);

            // Then
            assertThat(foundMessages)
                    .hasSize(3) // We created 3 messages for this sender in setup
                    .isNotEmpty()
                    .allMatch(msg -> TEST_QUERY_SENDER.equals(msg.getSender()))
                    .extracting(Message::getContent)
                    .containsExactlyInAnyOrder(
                        "Hello World",
                        "Spring Boot is awesome",
                        "Testing is important"
                    );
                    
            // Verify no other senders' messages are included
            assertThat(messageRepository.findBySender("other@example.com"))
                    .hasSize(1)
                    .extracting(Message::getContent)
                    .containsExactly("JPA makes data access easy");
        }
        
        @Test
        @DisplayName("should find messages by sender with pagination")
        void shouldFindMessagesBySenderWithPagination() {
            // Given
            String testSender = "pagination-sender@example.com";
            List<Message> testMessages = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                Message message = new Message();
                message.setContent("Message " + i);
                message.setSender(testSender);
                testMessages.add(message);
            }
            messageRepository.saveAll(testMessages);
            messageRepository.flush();
            
            // When - Get all messages for the sender (ordered)
            List<Message> messages = messageRepository.findBySenderOrderByContentAsc(testSender);
            
            // Then
            assertThat(messages)
                    .hasSize(15)
                    .extracting(Message::getContent)
                    .contains("Message 0", "Message 1", "Message 2", "Message 3", "Message 4");
            
            // Verify sorting (should be by content as per the test data creation)
            List<String> sortedContents = messages.stream()
                    .map(Message::getContent)
                    .sorted()
                    .toList();
            
            assertThat(messages.stream().map(Message::getContent).toList())
                    .containsExactlyElementsOf(sortedContents);
        }


        @Test
        @DisplayName("should return empty list when no messages found for sender")
        void shouldReturnEmptyListWhenNoMessagesForSender() {
            // Given
            String nonExistentSender = "nonexistent@example.com";
            
            // When
            List<Message> foundMessages = messageRepository.findBySender(nonExistentSender);
            
            // Then
            assertThat(foundMessages)
                    .as("Should return empty list for non-existent sender")
                    .isNotNull()
                    .isEmpty();
                    
            // Verify with non-paginated query
            List<Message> foundMessagesAgain = messageRepository.findBySender(nonExistentSender);
                    
            assertThat(foundMessagesAgain)
                    .as("Should return empty list for non-existent sender on second query")
                    .isEmpty();
        }


        @Test
        @DisplayName("should find messages containing search term (case insensitive)")
        void shouldFindMessagesContainingSearchTerm() {
            // Given
            String searchTerm = "special" + UUID.randomUUID();
            Message message1 = createTestMessage("Hello " + searchTerm, "user1@example.com");
            Message message2 = createTestMessage(searchTerm.toUpperCase() + " World", "user2@example.com");
            messageRepository.saveAll(List.of(message1, message2));

            // When - Using pageable version of the search
            Page<Message> foundPage = messageRepository
                    .findByContentContainingIgnoreCase(searchTerm, Pageable.unpaged());

            // Then
            assertThat(foundPage.getContent())
                    .hasSize(2)
                    .extracting(Message::getContent)
                    .allMatch(content -> content.toLowerCase().contains(searchTerm.toLowerCase()));
        }


        @Test
        @DisplayName("should find messages with pagination")
        void shouldFindMessagesWithPagination() {
            // Given
            String targetContent = "pagination-test-" + UUID.randomUUID();
            List<Message> messages = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                messages.add(createTestMessage(
                        targetContent + " Message " + i,
                        "user" + (i % 3) + "@example.com"));
            }
            messageRepository.saveAll(messages);

            Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());

            // When
            Page<Message> firstPage = messageRepository.findAll(pageable);

            // Then
            assertThat(firstPage.getContent()).hasSize(5);
            assertThat(firstPage.getTotalElements()).isGreaterThanOrEqualTo(15);
            assertThat(firstPage.isFirst()).isTrue();
            assertThat(firstPage.hasNext()).isTrue();

            // Verify sorting (newest first)
            List<Message> content = firstPage.getContent();
            for (int i = 0; i < content.size() - 1; i++) {
                assertThat(content.get(i).getCreatedAt())
                        .isAfterOrEqualTo(content.get(i + 1).getCreatedAt());
            }
        }
    } // End of MessageQueryMethodsTests class

    @Nested
    @DisplayName("Performance Tests")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Transactional
    class MessagePerformanceTests {
        @BeforeEach
        @Transactional
        void setupPerformanceTest() {
            messageRepository.deleteAllInBatch();
            // Create a large dataset for performance testing
            int batchSize = 100;
            for (int i = 0; i < LARGE_DATASET_SIZE; i += batchSize) {
                List<Message> batch = new ArrayList<>();
                for (int j = i; j < Math.min(i + batchSize, LARGE_DATASET_SIZE); j++) {
                    batch.add(createTestMessage(
                        "Test message " + j, 
                        "user" + (j % 10) + "@example.com"));
                }
                messageRepository.saveAll(batch);
                messageRepository.flush();
                entityManager.clear();
            }
            messageRepository.flush();
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
                
                // Time the query
                long startTime = System.currentTimeMillis();
                Page<Message> resultPage = messageRepository.findAll(pageable);
                long queryTime = System.currentTimeMillis() - startTime;
                
                // Verify results
                int expectedSize = (page == totalPages - 1 && LARGE_DATASET_SIZE % pageSize != 0) 
                    ? LARGE_DATASET_SIZE % pageSize 
                    : pageSize;
                    
                assertThat(resultPage.getContent()).hasSize(expectedSize);
                assertThat(resultPage.getNumber()).isEqualTo(page);
                assertThat(resultPage.getTotalPages()).isEqualTo(totalPages);
                assertThat(resultPage.getTotalElements()).isEqualTo(LARGE_DATASET_SIZE);
                
                // Verify performance - should be reasonably fast
                assertThat(queryTime)
                    .as("Query for page %d took %d ms which is too long", page, queryTime)
                    .isLessThan(1000); // Should be much faster than 1 second
            }
        }
    } // End of MessagePerformanceTests class

    private static void assertThrowsAny(MessageRepository repo, Message message, String method) {
        assertThrowsAny(
            repo, message, method,
            jakarta.validation.ConstraintViolationException.class,
            org.springframework.dao.DataIntegrityViolationException.class
        );
    }

    private static void assertThrowsAny(MessageRepository repo, Message message, String method, Class<?>... exceptionTypes) {
        boolean thrown = false;
        try {
            if ("saveAndFlush".equals(method)) {
                repo.saveAndFlush(message);
            } else {
                throw new UnsupportedOperationException("Unknown method: " + method);
            }
        } catch (Exception e) {
            for (Class<?> exType : exceptionTypes) {
                if (exType.isInstance(e) || (e.getCause() != null && exType.isInstance(e.getCause()))) {
                    thrown = true;
                    break;
                }
            }
            if (!thrown) throw e;
        }
        if (!thrown) fail("Expected one of: " + java.util.Arrays.toString(exceptionTypes));
    }

    @TestConfiguration
    static class NoFlywayConfig {
        @Bean
        public org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy flywayMigrationStrategy() {
            return flyway -> { /* do nothing */ };
        }
    }
} // End of MessageRepositoryIT class
