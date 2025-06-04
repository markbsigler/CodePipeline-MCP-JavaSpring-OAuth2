package com.codepipeline.mcp.service;

import com.codepipeline.mcp.exception.ResourceNotFoundException;
import com.codepipeline.mcp.model.Message;
import com.codepipeline.mcp.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Message Service Unit Tests")
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    private Message testMessage;
    private final String messageId = UUID.randomUUID().toString();
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        testMessage = new Message();
        testMessage.setId(messageId);
        testMessage.setContent("Test message");
        testMessage.setSender("testuser");
        testMessage.setCreatedAt(now);
        testMessage.setUpdatedAt(now);
        testMessage.setVersion(0L);
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        
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
            assertThat(result).hasSize(1).contains(testMessage);
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
            String nonExistentId = UUID.randomUUID().toString();
            when(messageRepository.findById(nonExistentId)).thenReturn(Optional.empty());
            
            // When/Then
            assertThatThrownBy(() -> messageService.findById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Message not found with id: " + nonExistentId);
        }
        
        @Test
        @DisplayName("should find messages by sender")
        void shouldFindMessagesBySender() {
            // Given
            when(messageRepository.findBySender("testuser")).thenReturn(List.of(testMessage));
            
            // When
            List<Message> messages = messageService.findBySender("testuser");
            
            // Then
            assertThat(messages).hasSize(1).contains(testMessage);
            verify(messageRepository).findBySender("testuser");
        }
        
        @Nested
        @DisplayName("search")
        class SearchTests {
            
            @Test
            @DisplayName("Should search messages by content")
            void shouldSearchMessagesByContent() {
                // Given
                String searchQuery = "test";
                Pageable pageable = PageRequest.of(0, 10);

                Page<Message> expectedPage = new PageImpl<>(List.of(testMessage), pageable, 1);
                when(messageRepository.findByContentContainingIgnoreCase(eq(searchQuery), any(Pageable.class)))
                        .thenReturn(expectedPage);
                
                // When
                Page<Message> result = messageService.search(searchQuery, pageable);

                // Then
                assertThat(result.getContent()).hasSize(1).contains(testMessage);
                verify(messageRepository).findByContentContainingIgnoreCase(eq(searchQuery), any(Pageable.class));
            }
        }
    }
    
    @Nested
    @DisplayName("Create Operations")
    class CreateOperations {
        
        @Test
        @DisplayName("should create message")
        void shouldCreateMessage() {
            // Given
            Message newMessage = new Message();
            newMessage.setContent("New message");
            newMessage.setSender("newuser");
                    
            Message savedMessage = new Message();
            savedMessage.setId(messageId);
            savedMessage.setContent("New message");
            savedMessage.setSender("newuser");
            savedMessage.setCreatedAt(now);
            savedMessage.setUpdatedAt(now);
            when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
            
            // When
            Message created = messageService.create(newMessage);
            
            // Then
            assertThat(created.getId()).isNotNull();
            assertThat(created.getContent()).isEqualTo("New message");
            assertThat(created.getSender()).isEqualTo("newuser");
            assertThat(created.getCreatedAt()).isNotNull();
            assertThat(created.getUpdatedAt()).isNotNull();
            verify(messageRepository).save(any(Message.class));
        }
    }
    
    @Nested
    @DisplayName("Update Operations")
    class UpdateOperations {
        
        @Test
        @DisplayName("should update message")
        void shouldUpdateMessage() {
            // Given
            Message updatedMessage = new Message();
            updatedMessage.setContent("Updated content");
            updatedMessage.setSender("updated@example.com");
            
            Message existingMessage = new Message();
            existingMessage.setId(messageId);
            existingMessage.setContent("Original content");
            existingMessage.setSender("original@example.com");
            existingMessage.setCreatedAt(now);
            existingMessage.setUpdatedAt(now);
            
            when(messageRepository.findById(messageId)).thenReturn(Optional.of(existingMessage));
            when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // When
            Message result = messageService.update(messageId, updatedMessage);
            
            // Then
            assertThat(result.getContent()).isEqualTo(updatedMessage.getContent());
            assertThat(result.getSender()).isEqualTo(updatedMessage.getSender());
            assertThat(result.getId()).isEqualTo(messageId);
            verify(messageRepository).findById(messageId);
            verify(messageRepository).save(any(Message.class));
        }
        
        @Test
        @DisplayName("should throw exception when updating non-existent message")
        void shouldThrowExceptionWhenUpdatingNonExistentMessage() {
            // Given
            String nonExistentId = UUID.randomUUID().toString();
            when(messageRepository.findById(nonExistentId)).thenReturn(Optional.empty());
            
            // When/Then
            assertThatThrownBy(() -> messageService.update(nonExistentId, testMessage))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Message not found with id: " + nonExistentId);
            verify(messageRepository, never()).save(any(Message.class));
        }
    }
    
    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        
        @Test
        @DisplayName("should delete message")
        void shouldDeleteMessage() {
            // Given
            when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage));
            doNothing().when(messageRepository).delete(testMessage);
            
            // When
            messageService.delete(messageId);
            
            // Then
            verify(messageRepository).findById(messageId);
            verify(messageRepository).delete(testMessage);
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
                    .hasMessage("Message not found with id: " + nonExistentId);
            verify(messageRepository, never()).delete(any(Message.class));
        }
    }
}
