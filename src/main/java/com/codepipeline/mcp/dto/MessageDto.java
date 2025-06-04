package com.codepipeline.mcp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.codepipeline.mcp.model.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDto {

    @Schema(description = "Unique identifier of the message", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @NotBlank(message = "Content is required")
    @Size(max = 500, message = "Content must be less than 500 characters")
    @Schema(description = "Content of the message", example = "Hello, World!", required = true)
    private String content;

    @Schema(description = "Sender of the message", example = "user@example.com")
    private String sender;

    @Schema(description = "Timestamp when the message was created", example = "2023-01-01T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the message was last updated", example = "2023-01-01T12:05:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "Version for optimistic locking")
    private Long version;

    public static MessageDto fromEntity(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .sender(message.getSender())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .version(message.getVersion())
                .build();
    }

    public static Message toEntity(MessageDto dto) {
        return Message.builder()
                .id(dto.getId())
                .content(dto.getContent())
                .sender(dto.getSender())
                .build();
    }
}
