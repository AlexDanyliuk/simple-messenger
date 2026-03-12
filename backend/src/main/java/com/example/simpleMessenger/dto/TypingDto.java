package com.example.simpleMessenger.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingDto {
    private Long senderId;
    private Long recipientId;
    private boolean typing;
}
