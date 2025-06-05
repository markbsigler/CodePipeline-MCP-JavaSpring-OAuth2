package com.codepipeline.mcp.service;

import com.codepipeline.mcp.exception.ResourceNotFoundException;
import com.codepipeline.mcp.model.Message;
import com.codepipeline.mcp.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("Message Service Unit Tests")
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    private Message testMessage;
    private String messageId;
    private LocalDateTime now;
    private static final String TEST_SENDER = "testuser";
    private static final String TEST_CONTENT = "Test message content";

    @BeforeEach
    void setUp() {
        messageId = UUID.randomUUID().toString();
        now = LocalDateTime.now();
        
        testMessage = Message.builder()
                .id(messageId)
                .content(TEST_CONTENT)
                .sender(TEST_SENDER)
                .createdAt(now)
                .updatedAt(now)
                .version(0L)
                .build();
                
        // Reset mocks before each test
        reset(messageRepository);
    }
    
    // Helper method for save behavior
    private void setupSaveBehavior() {
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            // Create a new message with the same properties to simulate JPA behavior
            Message savedMessage = Message.builder()
                    .id(msg.getId() != null ? msg.getId() : UUID.randomUUID().toString())
                    .content(msg.getContent())
                    .sender(msg.getSender())
                    .createdAt(msg.getCreatedAt() != null ? msg.getCreatedAt() : LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .version(msg.getVersion() != null ? msg.getVersion() + 1 : 0)
                    .build();
            return savedMessage;
        });
    }

    @Nested
    @DisplayName("Create Operations")
    class CreateOperations {
        
        @Test
        @DisplayName("should create a message")
        void shouldCreateMessage() {
            // Given
            Message message = Message.builder()
                    .content("Test content")
                    .sender("testuser")
                    .build();
            
            Message savedMessage = Message.builder()
                    .id(messageId)
                    .content("Test content")
                    .sender("testuser")
                    .createdAt(LocalDateTime.now())
                    .version(0L)
                    .build();
            
            when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
            
            // When
            Message createdMessage = messageService.create(message);
            
            // Then
            assertThat(createdMessage).isNotNull();
            assertThat(createdMessage.getId()).isEqualTo(messageId);
            assertThat(createdMessage.getContent()).isEqualTo("Test content");
            assertThat(createdMessage.getSender()).isEqualTo("testuser");
            assertThat(createdMessage.getCreatedAt()).isNotNull();
            
            verify(messageRepository).save(any(Message.class));
        }
        
        @Test
        @DisplayName("should throw exception when creating null message")
        void shouldThrowExceptionWhenCreatingNullMessage() {
            // When/Then
            assertThatThrownBy(() -> messageService.create(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Message cannot be null");
            
            verifyNoInteractions(messageRepository);
        }
        
        @Test
        @DisplayName("should throw exception when creating message with null or empty content")
        void shouldThrowExceptionWhenCreatingMessageWithNullOrEmptyContent() {
            // Given
            Message message = Message.builder()
                    .content(null)
                    .sender("testuser")
                    .build();
            
            // When/Then
            assertThatThrownBy(() -> messageService.create(message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Message content cannot be null or empty");
            
            // Test empty content
            message.setContent("");
            assertThatThrownBy(() -> messageService.create(message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Message content cannot be null or empty");
            
            // Test blank content
            message.setContent("   ");
            assertThatThrownBy(() -> messageService.create(message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Message content cannot be null or empty");
            
            verifyNoInteractions(messageRepository);
        }
        
        @Test
        @DisplayName("should throw exception when creating message with null or empty sender")
        void shouldThrowExceptionWhenCreatingMessageWithNullOrEmptySender() {
            // Given
            Message message = Message.builder()
                    .content("Test content")
                    .sender(null)
                    .build();
            
            // When/Then
            assertThatThrownBy(() -> messageService.create(message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Message sender cannot be null or empty");
            
            // Test empty sender
            message.setSender("");
            assertThatThrownBy(() -> messageService.create(message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Message sender cannot be null or empty");
            
            // Test blank sender
            message.setSender("   ");
            assertThatThrownBy(() -> messageService.create(message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Message sender cannot be null or empty");
            
            verifyNoInteractions(messageRepository);
        }
    }
    
    @Nested
    @DisplayName("Update Operations")
    class UpdateOperations {
        
        @Test
        @DisplayName("should update message successfully")
        void shouldUpdateMessage() {
            // Given
            setupSaveBehavior();
            Message existingMessage = Message.builder()
                    .id(messageId)
                    .content("Old content")
                    .sender("olduser")
                    .createdAt(now.minusDays(1))
                    .updatedAt(now.minusDays(1))
                    .version(0L)
                    .build();
                    
            Message updateData = Message.builder()
                    .content("Updated content")
                    .sender("newuser")
                    .build();
            
            when(messageRepository.findById(messageId)).thenReturn(Optional.of(existingMessage));
            
            // When
            Message result = messageService.update(messageId, updateData);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(messageId);
            assertThat(result.getContent()).isEqualTo("Updated content");
            assertThat(result.getSender()).isEqualTo("newuser");
            assertThat(result.getVersion()).isEqualTo(1L); // Version should be incremented in this update
            assertThat(result.getCreatedAt()).isEqualTo(existingMessage.getCreatedAt());
            // Verify updatedAt was set (not null)
            assertThat(result.getUpdatedAt()).isNotNull();
            // Verify updatedAt is not before the original updatedAt time
            assertThat(result.getUpdatedAt()).isAfterOrEqualTo(existingMessage.getUpdatedAt());
            
            verify(messageRepository).findById(messageId);
            verify(messageRepository).save(any(Message.class));
            verifyNoMoreInteractions(messageRepository);
        }
        
        @Test
        @DisplayName("should throw exception when updating non-existent message")
        void shouldThrowExceptionWhenUpdatingNonExistentMessage() {
            // Given
            String nonExistentId = UUID.randomUUID().toString();
            when(messageRepository.findById(nonExistentId)).thenReturn(Optional.empty());
            
            Message updateData = Message.builder()
                    .content("Updated content")
                    .sender("newuser")
                    .build();
            
            // When/Then
            assertThatThrownBy(() -> messageService.update(nonExistentId, updateData))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Message not found with id: '" + nonExistentId + "'");
                    
            verify(messageRepository).findById(nonExistentId);
            verify(messageRepository, never()).save(any(Message.class));
        }
        
        @Test
        @DisplayName("should handle optimistic locking conflict")
        void shouldHandleOptimisticLockingConflict() {
            // Given
            Message existingMessage = Message.builder()
                    .id(messageId)
                    .content("Original content")
                    .sender("user1")
                    .version(1L)
                    .build();
                    
            Message updateData = Message.builder()
                    .content("Updated content")
                    .sender("user1")
                    .version(0L) // Stale version
                    .build();
                    
            when(messageRepository.findById(messageId)).thenReturn(Optional.of(existingMessage));
            when(messageRepository.save(any(Message.class)))
                    .thenThrow(new OptimisticLockingFailureException("Version conflict"));
            
            // When/Then
            assertThatThrownBy(() -> messageService.update(messageId, updateData))
                    .isInstanceOf(OptimisticLockingFailureException.class)
                    .hasMessageContaining("Version conflict");
                    
            verify(messageRepository).findById(messageId);
            verify(messageRepository).save(any(Message.class));
            verifyNoMoreInteractions(messageRepository);
        }
    }
    
    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        
        @Test
        @DisplayName("should delete message successfully")
        void shouldDeleteMessage() {
            // Given
            when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage));
            doNothing().when(messageRepository).delete(testMessage);
            
            // When
            messageService.delete(messageId);
            
            // Then
            verify(messageRepository).findById(messageId);
            verify(messageRepository).delete(testMessage);
            verifyNoMoreInteractions(messageRepository);
        }
        
        @Test
        @DisplayName("should throw exception when deleting non-existent message")
        void shouldThrowExceptionWhenDeletingNonExistentMessage() {
            // Given
            String nonExistentId = UUID.randomUUID().toString();
            when(messageRepository.findById(nonExistentId)).thenReturn(Optional.empty());
            
            // When/Then
            assertThatThrownBy(() -> messageService.delete(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Message not found with id: '" + nonExistentId + "'");
                    
            verify(messageRepository).findById(nonExistentId);
            verify(messageRepository, never()).delete(any(Message.class));
        }
        
        @Test
        @DisplayName("should handle concurrent deletion")
        void shouldHandleConcurrentDeletion() throws InterruptedException, ExecutionException, TimeoutException {
            // Given
            int threadCount = 5;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(1);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger exceptionCount = new AtomicInteger(0);
            
            // Create a copy of the test message for verification
            Message testMessageCopy = Message.builder()
                    .id(testMessage.getId())
                    .content(testMessage.getContent())
                    .sender(testMessage.getSender())
                    .createdAt(testMessage.getCreatedAt())
                    .version(testMessage.getVersion())
                    .build();
            
            // First call to findById returns the message, subsequent calls throw ResourceNotFoundException
            when(messageRepository.findById(messageId))
                    .thenReturn(Optional.of(testMessageCopy))
                    .thenThrow(new ResourceNotFoundException("Message", "id", messageId));
            
            // Delete operation will be called once
            doAnswer(invocation -> {
                // Simulate some work
                Thread.sleep(50);
                return null;
            }).when(messageRepository).delete(any(Message.class));
            
            // When - submit all tasks
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                Future<?> future = executorService.submit(() -> {
                    try {
                        latch.await();
                        messageService.delete(messageId);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                    }
                });
                futures.add(future);
            }
            
            // Start all threads at once
            latch.countDown();
            
            // Wait for all tasks to complete with a timeout
            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
            
            // Shutdown the executor
            executorService.shutdown();
            
            // Then
            // Verify that exactly one thread succeeded in deleting the message
            assertThat(successCount.get()).as("Number of successful deletions").isEqualTo(1);
            
            // Verify that delete was only called once despite multiple attempts
            verify(messageRepository, times(1)).delete(any(Message.class));
            
            // Verify that findById was called at least as many times as the number of threads
            verify(messageRepository, atLeast(threadCount)).findById(messageId);
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        // ... (rest of the code remains the same)
        @Test
        @DisplayName("should find all messages")
        void shouldFindAllMessages() {
            // Given
            when(messageRepository.findAll()).thenReturn(List.of(testMessage));

            // When
            List<Message> messages = messageService.findAll();

            // Then
            assertThat(messages).hasSize(1).contains(testMessage);
            verify(messageRepository).findAll();
            verifyNoMoreInteractions(messageRepository);
        }
        
        @Test
        @DisplayName("should find all messages with pagination")
        void shouldFindAllMessagesWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            Page<Message> messagePage = new PageImpl<>(List.of(testMessage), pageable, 1);
            when(messageRepository.findAll(pageable)).thenReturn(messagePage);

            // When
            Page<Message> result = messageService.findAll(pageable);

            // Then
            assertThat(result.getContent()).hasSize(1).contains(testMessage);
            verify(messageRepository).findAll(pageable);
        }
        
        @Test
        @DisplayName("should find message by id")
        void shouldFindMessageById() {
            // Given
            when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage));

            // When
            Message found = messageService.findById(messageId);

            // Then
            assertThat(found).isEqualTo(testMessage);
            verify(messageRepository).findById(messageId);
        }
        
        @Test
        @DisplayName("should throw exception when message not found")
        void shouldThrowExceptionWhenMessageNotFound() {
            // Given
            String nonExistentId = "non-existent-id";
            when(messageRepository.findById(nonExistentId)).thenReturn(Optional.empty());
            
            // When/Then
            assertThatThrownBy(() -> messageService.findById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Message not found with id: '" + nonExistentId + "'");
            
            verify(messageRepository).findById(nonExistentId);
            verifyNoMoreInteractions(messageRepository);
        }
        
        @Test
        @DisplayName("should find messages by sender")
        void shouldFindMessagesBySender() {
            // Given
            when(messageRepository.findBySender(TEST_SENDER)).thenReturn(List.of(testMessage));
            
            // When
            List<Message> messages = messageService.findBySender(TEST_SENDER);
            
            // Then
            assertThat(messages).hasSize(1).contains(testMessage);
            verify(messageRepository).findBySender(TEST_SENDER);
        }
        
        @Test
        @DisplayName("should return empty list when no messages found for sender")
        void shouldReturnEmptyListWhenNoMessagesForSender() {
            // Given
            when(messageRepository.findBySender(anyString())).thenReturn(List.of());
            
            // When
            List<Message> messages = messageService.findBySender("nonexistent");
            
            // Then
            assertThat(messages).isEmpty();
            verify(messageRepository).findBySender("nonexistent");
        }
        
        @Test
        @DisplayName("should search messages by content")
        void shouldSearchMessagesByContent() {
            // Given
            String searchTerm = "test";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Message> expectedPage = new PageImpl<>(List.of(testMessage), pageable, 1);
            when(messageRepository.findByContentContainingIgnoreCase(searchTerm, pageable))
                    .thenReturn(expectedPage);
            
            // When
            Page<Message> result = messageService.search(searchTerm, pageable);
            
            // Then
            assertThat(result.getContent()).hasSize(1).contains(testMessage);
            verify(messageRepository).findByContentContainingIgnoreCase(searchTerm, pageable);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("should handle null or empty search term")
        void shouldHandleNullOrEmptySearchTerm(String searchTerm) {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            Page<Message> messagePage = new PageImpl<>(List.of(testMessage), pageable, 1);
            
            // Mock the repository to return the test page for any search term
            when(messageRepository.findByContentContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(messagePage);
            
            // When
            Page<Message> result = messageService.search(searchTerm, pageable);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1).contains(testMessage);
            
            // Verify repository interaction - should call with the exact search term (service doesn't trim)
            verify(messageRepository).findByContentContainingIgnoreCase(searchTerm, pageable);
            verifyNoMoreInteractions(messageRepository);
        }
    }
}
