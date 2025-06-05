package com.codepipeline.mcp.util;

import com.codepipeline.mcp.model.Message;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestDataFactory {
    
    private TestDataFactory() {
        // Private constructor to prevent instantiation
    }
    
    public static Message createMessage() {
        return createMessage("Test message", "testuser@example.com");
    }
    
    public static Message createMessage(String content, String sender) {
        return Message.builder()
                .content(content)
                .sender(sender)
                .build();
    }
    
    public static List<Message> createMessages(int count, String contentPrefix, String sender) {
        return IntStream.range(0, count)
                .mapToObj(i -> createMessage(contentPrefix + " " + (i + 1), sender))
                .collect(Collectors.toList());
    }
    
    public static String randomId() {
        return UUID.randomUUID().toString();
    }
    
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
