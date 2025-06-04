package com.codepipeline.mcp.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt != null ? jwt.getClaimAsString("preferred_username") : "anonymous";
        log.info("Received greeting from: {}", username);
        return new Greeting("Hello, " + message.name() + "!");
    }

    // Example of sending message to a specific user
    @MessageMapping("/private-message")
    public void sendPrivateMessage(PrivateMessage message, @AuthenticationPrincipal Jwt jwt) {
        String sender = jwt != null ? jwt.getClaimAsString("preferred_username") : "system";
        log.info("Private message from {} to {}: {}", sender, message.recipient(), message.content());
        
        // Send to specific user
        messagingTemplate.convertAndSendToUser(
            message.recipient(),
            "/queue/private",
            new PrivateMessage(sender, message.recipient(), message.content())
        );
    }

    // DTOs for WebSocket messages
    public record HelloMessage(String name) {}
    public record Greeting(String content) {}
    public record PrivateMessage(String sender, String recipient, String content) {}
    
    // Notification DTO
    public record Notification(String from, String message) {}
}
