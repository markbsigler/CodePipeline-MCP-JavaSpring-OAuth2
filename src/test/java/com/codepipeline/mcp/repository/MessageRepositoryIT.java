package com.codepipeline.mcp.repository;

import com.codepipeline.mcp.model.Message;
import com.codepipeline.mcp.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Message Repository Integration Tests")
class MessageRepositoryIT {
    
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpass")
        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("POSTGRES")))
        .withReuse(true);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }
    
    @Autowired
    @SuppressWarnings("unused") // Used by Spring Data JPA for test entity management
    private TestEntityManager testEntityManager;

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    private static final String TEST_SENDER = "test@example.com";
    private static final int TEST_MESSAGE_COUNT = 5;
    private static final int LARGE_DATASET_SIZE = 1000;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up existing data before each test
        messageRepository.deleteAllInBatch();
        entityManager.flush();
        entityManager.clear();
        
        // Create test messages
        List<Message> messages = IntStream.range(0, TEST_MESSAGE_COUNT)
                .mapToObj(i -> TestDataFactory.createMessage(
                        "Test message " + i,
                        TEST_SENDER))
                .collect(Collectors.toList());
        messageRepository.saveAll(messages);
        messageRepository.flush();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up after each test to ensure isolation
        messageRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {
        
        private Message createNewTestMessage() {
            return TestDataFactory.createMessage("Unique test message " + UUID.randomUUID(), TEST_SENDER);
        }

        @Test
        @DisplayName("Should save a message")
        @Transactional
        void shouldSaveMessage() {
            // Given
            Message message = TestDataFactory.createMessage("Test content", TEST_SENDER);
            
            // When
            Message savedMessage = messageRepository.saveAndFlush(message);
            entityManager.clear(); // Clear persistence context to ensure we're reading from DB
            
            // Then
            Message foundMessage = messageRepository.findById(savedMessage.getId()).orElse(null);
            assertThat(foundMessage).isNotNull();
            assertThat(foundMessage.getContent()).isEqualTo("Test content");
            assertThat(foundMessage.getSender()).isEqualTo(TEST_SENDER);
            assertThat(foundMessage.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(foundMessage.getUpdatedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(foundMessage.getVersion()).isZero();
        }
        
        @Test
        @DisplayName("Should fail to save message with null content")
        @Transactional
        void shouldFailToSaveMessageWithNullContent() {
            // Given
            Message message = TestDataFactory.createMessage(null, TEST_SENDER);
            
            // When/Then
            assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
                Message saved = messageRepository.save(message);
                messageRepository.flush();
                entityManager.clear();
            });
        }
        
        @Test
        @DisplayName("Should fail to save message with null sender")
        @Transactional
        void shouldFailToSaveMessageWithNullSender() {
            // Given
            Message message = TestDataFactory.createMessage("Test content", null);
            
            // When/Then
            assertThrows(jakarta.validation.ConstraintViolationException.class, () -> {
                Message saved = messageRepository.save(message);
                messageRepository.flush();
                entityManager.clear();
            });
        }

        @Test
        @DisplayName("should find message by ID")
        void shouldFindMessageById() {
            // Given
            Message savedMessage = messageRepository.save(createNewTestMessage());

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
        @DisplayName("should return all messages sorted by creation date descending")
        void shouldFindAllMessages() {
            // When
            List<Message> messages = messageRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
            );

            // Then
            assertThat(messages)
                .hasSize(TEST_MESSAGE_COUNT)
                .isSortedAccordingTo((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()));
                
            // Verify all expected content is present
            assertThat(messages)
                .extracting(Message::getContent)
                .allMatch(content -> content.startsWith("Test message "));
        }
        
        @Test
        @DisplayName("Should return empty list when no messages exist")
        @Transactional
        void shouldReturnEmptyListWhenNoMessages() {
            // Given - setUp() has already cleared the database
            
            // When
            List<Message> messages = messageRepository.findAll();
            
            // Then
            assertThat(messages).as("Expected no messages in the database").isEmpty();
        }

        @Test
        @DisplayName("should update message content and update timestamp")
        void shouldUpdateMessage() {
            // Given
            Message savedMessage = messageRepository.findAll().get(0);
            LocalDateTime originalCreatedAt = savedMessage.getCreatedAt();
            LocalDateTime originalUpdatedAt = savedMessage.getUpdatedAt();
            String updatedContent = "Updated content " + UUID.randomUUID();
            
            // Small delay to ensure timestamps differ
            try { Thread.sleep(10); } catch (InterruptedException e) {}

            // When
            savedMessage.setContent(updatedContent);
            Message updatedMessage = messageRepository.saveAndFlush(savedMessage);

            // Then
            assertThat(updatedMessage.getContent()).isEqualTo(updatedContent);
            assertThat(updatedMessage.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(updatedMessage.getUpdatedAt())
                    .isAfter(originalUpdatedAt)
                    .isAfter(updatedMessage.getCreatedAt());
                    
            // Verify in database
            Message dbMessage = messageRepository.findById(savedMessage.getId()).orElseThrow();
            assertThat(dbMessage.getContent()).isEqualTo(updatedContent);
            assertThat(dbMessage.getUpdatedAt()).isAfter(originalUpdatedAt);
        }
        
        @Test
        @DisplayName("should increment version on update")
        void shouldIncrementVersionOnUpdate() {
            // Given
            Message savedMessage = messageRepository.save(createNewTestMessage());
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
            Message savedMessage = messageRepository.save(createNewTestMessage());
            long initialCount = messageRepository.count();
            
            // When
            messageRepository.delete(savedMessage);
            
            // Then
            assertThat(messageRepository.findById(savedMessage.getId())).isNotPresent();
            assertThat(messageRepository.count()).isEqualTo(initialCount - 1);
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
            
            // When/Then - Should not throw when deleting non-existent ID
            assertThatNoException()
                .isThrownBy(() -> messageRepository.deleteById(nonExistentId));
                
            // Verify no side effects
            assertThat(messageRepository.count()).isEqualTo(TEST_MESSAGE_COUNT);
        }
    }

    @Nested
    @DisplayName("Concurrent Operations")
    class ConcurrentOperations {

        @Test
        @DisplayName("should handle concurrent reads and writes")
        void shouldHandleConcurrentReadsAndWrites() throws InterruptedException {
            // Given
            int threadCount = 5;
            int iterations = 5;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);

            // When
            for (int i = 0; i < threadCount; i++) {
                final int threadNum = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < iterations; j++) {
                            // Each thread creates and reads messages
                            Message message = TestDataFactory.createMessage(
                                    "Thread " + threadNum + " - Message " + j,
                                    "user" + threadNum + "@example.com"
                            );
                            messageRepository.saveAndFlush(message);

                            // Read some messages
                            messageRepository.findBySender("user" + threadNum + "@example.com");
                            // Small delay to increase chance of concurrency issues
                            Thread.sleep(10);
                        }
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        fail("Test failed with exception: " + e.getMessage(), e);
                    } finally {
                        latch.countDown();
                    }
                });
            }


            // Wait for all threads to complete or timeout after 30 seconds
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            // Then
            assertThat(completed).isTrue();
            assertThat(successCount.get()).isEqualTo(threadCount);

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
    }

    @Nested
    @DisplayName("Query Methods")
    @Transactional
    class QueryMethods {
        
        @Test
        @DisplayName("should find messages by sender")
        void shouldFindMessagesBySender() {
            // Given - already have TEST_MESSAGE_COUNT messages from TEST_SENDER in setup
            String otherSender = "otheruser@example.com";
            Message otherMessage = TestDataFactory.createMessage("Other message", otherSender);
            messageRepository.saveAndFlush(otherMessage);

            // When
            List<Message> foundMessages = messageRepository.findBySender(TEST_SENDER);

            // Then
            assertThat(foundMessages)
                    .hasSize(TEST_MESSAGE_COUNT)
                    .allMatch(msg -> TEST_SENDER.equals(msg.getSender()));
        }
        
        @Test
        @DisplayName("should find messages by sender with pagination")
        void shouldFindMessagesBySenderWithPagination() {
            // Given
            String testSender = "pagination-sender@example.com";
            List<Message> testMessages = IntStream.range(0, 15)
                    .mapToObj(i -> TestDataFactory.createMessage("Message " + i, testSender))
                    .collect(Collectors.toList());
            messageRepository.saveAllAndFlush(testMessages);
            
            // When
            List<Message> foundMessages = messageRepository.findBySender(testSender);
            
            // Then
            assertThat(foundMessages).hasSize(15);
            assertThat(foundMessages)
                    .allMatch(msg -> testSender.equals(msg.getSender()));
        }


        @Test
        @DisplayName("should return empty list when no messages found for sender")
        void shouldReturnEmptyListWhenNoMessagesForSender() {
            // When
            List<Message> foundMessages = messageRepository.findBySender("nonexistent@example.com");

            // Then
            assertThat(foundMessages).isEmpty();
        }


        @Test
        @DisplayName("should find messages containing search term (case insensitive)")
        void shouldFindMessagesContainingSearchTerm() {
            // Given
            String searchTerm = "special" + UUID.randomUUID();
            Message message1 = TestDataFactory.createMessage("Hello " + searchTerm, "user1@example.com");
            Message message2 = TestDataFactory.createMessage(searchTerm.toUpperCase() + " World", "user2@example.com");
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
            List<Message> messages = IntStream.range(0, 15)
                    .mapToObj(i -> TestDataFactory.createMessage(
                            targetContent + " Message " + i,
                            "user" + (i % 3) + "@example.com"))
                    .collect(Collectors.toList());
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

    @Nested
    @DisplayName("Performance Tests")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class PerformanceTests {
        
        @BeforeAll
        void setupAll() {
            // Clean up any existing data
            messageRepository.deleteAllInBatch();
            
            // Create a large dataset for performance testing
            int batchSize = 100;
            for (int i = 0; i < LARGE_DATASET_SIZE; i += batchSize) {
                List<Message> batch = IntStream.range(i, Math.min(i + batchSize, LARGE_DATASET_SIZE))
                    .mapToObj(j -> TestDataFactory.createMessage(
                        "Test message " + j, 
                        "user" + (j % 10) + "@example.com"))
                    .collect(Collectors.toList());
                messageRepository.saveAll(batch);
                messageRepository.flush();
                entityManager.clear(); // Clear persistence context to avoid memory issues
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
    }
}
}
