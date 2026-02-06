package com.example.simpleMessenger.Chat.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatNotification {

    private Long Id;

    private String senderId;
    private String recipientId;
    private String content;
}
