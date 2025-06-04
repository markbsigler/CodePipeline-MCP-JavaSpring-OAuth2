package com.codepipeline.mcp.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = getUsernameFromHeader(headerAccessor);
        
        log.info("User connected: {}", username);
        
        // Notify all clients about the new user
        WebSocketController.Notification notification = new WebSocketController.Notification(
            "SYSTEM",
            String.format("User %s joined the chat", username)
        );
        
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = getUsernameFromHeader(headerAccessor);
        
        if (username != null) {
            log.info("User disconnected: {}", username);
            
            // Notify all clients about the user leaving
            WebSocketController.Notification notification = new WebSocketController.Notification(
                "SYSTEM",
                String.format("User %s left the chat", username)
            );
            
            messagingTemplate.convertAndSend("/topic/notifications", notification);
        }
    }
    
    private String getUsernameFromHeader(StompHeaderAccessor headerAccessor) {
        Authentication auth = (Authentication) headerAccessor.getUser();
        if (auth != null && auth.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) auth.getPrincipal();
            return jwt.getClaimAsString("preferred_username");
        }
        return "anonymous";
    }
}
