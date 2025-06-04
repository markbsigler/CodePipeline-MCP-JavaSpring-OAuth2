package com.codepipeline.mcp.util;

import com.codepipeline.mcp.dto.MessageDto;
import com.codepipeline.mcp.model.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for creating test data.
 */
public class TestDataUtil {
    
    private TestDataUtil() {
        // Private constructor to prevent instantiation
    }
    
    public static Message createTestMessage() {
        return createTestMessage("Test Message");
    }
    
    public static Message createTestMessage(String content) {
        return createTestMessage(content, "testuser@example.com");
    }
    
    public static Message createTestMessage(String content, String sender) {
        return Message.builder()
                .id(UUID.randomUUID().toString())
                .content(content)
                .sender(sender)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(0L)
                .build();
    }
    
    public static MessageDto createTestMessageDto() {
        return createTestMessageDto("Test Message");
    }
    
    public static MessageDto createTestMessageDto(String content) {
        return createTestMessageDto(content, "testuser@example.com");
    }
    
    public static MessageDto createTestMessageDto(String content, String sender) {
        return MessageDto.builder()
                .content(content)
                .sender(sender)
                .build();
    }
    
    public static List<Message> createTestMessages(int count, String contentPrefix) {
        return createTestMessages(count, contentPrefix, "testuser@example.com");
    }
    
    public static List<Message> createTestMessages(int count, String contentPrefix, String sender) {
        return IntStream.range(0, count)
                .mapToObj(i -> createTestMessage(contentPrefix + " " + (i + 1), sender))
                .collect(Collectors.toList());
    }
    
    public static List<MessageDto> createTestMessageDtos(int count, String contentPrefix) {
        return createTestMessageDtos(count, contentPrefix, "testuser@example.com");
    }
    
    public static List<MessageDto> createTestMessageDtos(int count, String contentPrefix, String sender) {
        return IntStream.range(0, count)
                .mapToObj(i -> createTestMessageDto(contentPrefix + " " + (i + 1), sender))
                .collect(Collectors.toList());
    }
}
