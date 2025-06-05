package com.codepipeline.mcp.controller;

import com.codepipeline.mcp.BaseIntegrationTest;
import com.codepipeline.mcp.dto.MessageDto;
import com.codepipeline.mcp.model.Message;
import com.codepipeline.mcp.repository.MessageRepository;
import com.codepipeline.mcp.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WithMockUser(roles = "USER")
@DisplayName("Message Controller Integration Tests")
class MessageControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageRepository messageRepository;

    private Message testMessage;
    private String messageId;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        
        testMessage = TestDataFactory.createMessage("Test message", "testuser@example.com");
        testMessage = messageRepository.save(testMessage);
        messageId = testMessage.getId();
    }

    @Nested
    @DisplayName("GET /api/messages")
    class GetAllMessages {
        
        @Test
        @DisplayName("should return all messages with pagination")
        void shouldReturnAllMessages() throws Exception {
            // Given - Additional test data
            messageRepository.save(Message.builder()
                    .content("Another message")
                    .sender("anotheruser")
                    .build());

            // When
            ResultActions result = mockMvc.perform(get("/api/messages")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].content", is(not(empty()))))
                    .andExpect(jsonPath("$.content[0].sender", is(not(empty()))));
        }
    }

    @Nested
    @DisplayName("GET /api/messages/{id}")
    class GetMessageById {
        
        @Test
        @DisplayName("should return message by id")
        void shouldReturnMessageById() throws Exception {
            // When
            ResultActions result = mockMvc.perform(get("/api/messages/{id}", messageId)
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(messageId)))
                    .andExpect(jsonPath("$.content", is("Test message")))
                    .andExpect(jsonPath("$.sender", is("testuser")));
        }

        @Test
        @DisplayName("should return 404 when message not found")
        void shouldReturn404WhenMessageNotFound() throws Exception {
            // When
            String nonExistentId = UUID.randomUUID().toString();
            ResultActions result = mockMvc.perform(get("/api/messages/{id}", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            result.andExpect(status().isNotFound());
        }
    }


    @Nested
    @DisplayName("POST /api/messages")
    class CreateMessage {
        
        @Test
        @DisplayName("should create a new message")
        void shouldCreateMessage() throws Exception {
            // Given
            MessageDto messageDto = MessageDto.builder()
                    .content("New test message")
                    .sender("testuser@example.com")
                    .build();

            // When
            ResultActions result = mockMvc.perform(post("/api/messages")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(messageDto)));

            // Then
            result.andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.content", is("New test message")));

            // Verify in database
            List<Message> messages = messageRepository.findAll();
            assertThat(messages).hasSize(2);
            assertThat(messages.get(1).getContent()).isEqualTo("New test message");
        }

        
        @Test
        @DisplayName("should return 400 when content is empty")
        void shouldReturn400WhenContentIsEmpty() throws Exception {
            // Given
            MessageDto messageDto = MessageDto.builder().content("").build();

            // When
            ResultActions result = mockMvc.perform(post("/api/messages")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(messageDto)));

            // Then
            result.andExpect(status().isBadRequest());
        }
    }


    @Nested
    @DisplayName("PUT /api/messages/{id}")
    class UpdateMessage {
        
        @Test
        @DisplayName("should update an existing message")
        void shouldUpdateMessage() throws Exception {
            // Given
            MessageDto updateDto = MessageDto.builder()
                    .content("Updated content")
                    .sender("testuser@example.com")
                    .build();

            // When
            ResultActions result = mockMvc.perform(put("/api/messages/{id}", messageId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)));

            // Then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", is("Updated content")));

            // Verify in database
            Message updated = messageRepository.findById(messageId).orElseThrow();
            assertThat(updated.getContent()).isEqualTo("Updated content");
        }

        @Test
        @DisplayName("should return 403 when updating another user's message")
        @WithMockUser(username = "otheruser")
        void shouldReturn403WhenUpdatingAnotherUsersMessage() throws Exception {
            // Given - test message was created by "testuser" in setup
            MessageDto updateDto = MessageDto.builder()
                    .content("Unauthorized update")
                    .sender("otheruser@example.com")
                    .build();

            // When
            ResultActions result = mockMvc.perform(put("/api/messages/{id}", messageId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)));

            // Then
            result.andExpect(status().isForbidden());
        }
    }


    @Nested
    @DisplayName("DELETE /api/messages/{id}")
    class DeleteMessage {
        
        @Test
        @DisplayName("should delete a message")
        void shouldDeleteMessage() throws Exception {
            // When
            ResultActions result = mockMvc.perform(delete("/api/messages/{id}", messageId)
                    .with(csrf()));

            // Then
            result.andExpect(status().isNoContent());
            assertThat(messageRepository.existsById(messageId)).isFalse();
        }


        @Test
        @DisplayName("should return 403 when deleting another user's message")
        @WithMockUser(username = "otheruser")
        void shouldReturn403WhenDeletingAnotherUsersMessage() throws Exception {
            // When
            ResultActions result = mockMvc.perform(delete("/api/messages/{id}", messageId)
                    .with(csrf()));

            // Then
            result.andExpect(status().isForbidden());
            assertThat(messageRepository.existsById(messageId)).isTrue();
        }
    }


    @Nested
    @DisplayName("GET /api/messages/search")
    class SearchMessages {
        
        @Test
        @DisplayName("should search messages by content")
        void shouldSearchMessagesByContent() throws Exception {
            // Given - Additional test data
            messageRepository.save(Message.builder()
                    .content("Another test message")
                    .sender("anotheruser")
                    .build());

            // When
            ResultActions result = mockMvc.perform(get("/api/messages/search")
                    .param("query", "test")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }
    }
}
