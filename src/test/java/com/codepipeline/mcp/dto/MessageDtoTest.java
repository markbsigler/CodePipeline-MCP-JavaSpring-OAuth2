package com.codepipeline.mcp.dto;

import com.codepipeline.mcp.model.Message;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MessageDtoTest {

    @Test
    void testMessageDtoCreation() {
        // Given
        String id = "1";
        String content = "Test message";
        String sender = "testuser";
        LocalDateTime now = LocalDateTime.now();

        // When
        MessageDto messageDto = MessageDto.builder()
                .id(id)
                .content(content)
                .sender(sender)
                .createdAt(now)
                .build();

        // Then
        assertNotNull(messageDto);
        assertEquals(id, messageDto.getId());
        assertEquals(content, messageDto.getContent());
        assertEquals(sender, messageDto.getSender());
        assertEquals(now, messageDto.getCreatedAt());
        assertNull(messageDto.getUpdatedAt());
        assertNull(messageDto.getVersion());
    }

    @Test
    void testMessageDtoFromEntity() {
        // Given
        String id = "1";
        String content = "Test message";
        String sender = "testuser";
        LocalDateTime now = LocalDateTime.now();

        Message message = Message.builder()
                .id(id)
                .content(content)
                .sender(sender)
                .createdAt(now)
                .updatedAt(now)
                .version(1L)
                .build();

        // When
        MessageDto messageDto = MessageDto.fromEntity(message);

        // Then
        assertNotNull(messageDto);
        assertEquals(id, messageDto.getId());
        assertEquals(content, messageDto.getContent());
        assertEquals(sender, messageDto.getSender());
        assertEquals(now, messageDto.getCreatedAt());
        assertEquals(now, messageDto.getUpdatedAt());
        assertEquals(1L, messageDto.getVersion());
    }
}