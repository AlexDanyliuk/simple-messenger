package com.example.simpleMessenger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageReactionDto {

    @NotNull(message = "Message id is required")
    private Long messageId;

    @NotBlank(message = "Emoji is required")
    private String emoji;
}
