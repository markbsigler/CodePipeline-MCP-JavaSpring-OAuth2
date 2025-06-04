package com.codepipeline.mcp.controller;

import com.codepipeline.mcp.dto.MessageDto;
import com.codepipeline.mcp.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Message management APIs")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all messages with pagination")
    public ResponseEntity<Page<MessageDto>> getAllMessages(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(messageService.findAll(pageable).map(MessageDto::fromEntity));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get a message by ID")
    public ResponseEntity<MessageDto> getMessageById(@PathVariable String id) {
        return ResponseEntity.ok(MessageDto.fromEntity(messageService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create a new message")
    public ResponseEntity<MessageDto> createMessage(
            @Valid @RequestBody MessageDto messageDto,
            @AuthenticationPrincipal Jwt jwt) {
        
        // Set the sender from the authenticated user
        String username = jwt.getClaimAsString("preferred_username");
        messageDto.setSender(username);
        
        MessageDto createdMessage = MessageDto.fromEntity(
            messageService.create(MessageDto.toEntity(messageDto))
        );
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdMessage.getId())
            .toUri();
            
        return ResponseEntity.created(location).body(createdMessage);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update an existing message")
    public ResponseEntity<MessageDto> updateMessage(
            @PathVariable String id,
            @Valid @RequestBody MessageDto messageDto,
            @AuthenticationPrincipal Jwt jwt) {
                
        // Verify the message exists and belongs to the current user
        String username = jwt.getClaimAsString("preferred_username");
        MessageDto existingMessage = MessageDto.fromEntity(messageService.findById(id));
        
        if (!existingMessage.getSender().equals(username)) {
            return ResponseEntity.status(403).build();
        }
        
        messageDto.setId(id);
        messageDto.setSender(username); // Ensure sender can't be changed
        
        return ResponseEntity.ok(MessageDto.fromEntity(
            messageService.update(id, MessageDto.toEntity(messageDto))
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Delete a message")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
                
        // Verify the message exists and belongs to the current user
        String username = jwt.getClaimAsString("preferred_username");
        MessageDto existingMessage = MessageDto.fromEntity(messageService.findById(id));
        
        if (!existingMessage.getSender().equals(username)) {
            return ResponseEntity.status(403).build();
        }
        
        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Search messages by content")
    public ResponseEntity<Page<MessageDto>> searchMessages(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
            messageService.search(query, pageable).map(MessageDto::fromEntity)
        );
    }
}
