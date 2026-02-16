package com.example.simpleMessenger.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
    private Long recipientId;
    private String content;
}