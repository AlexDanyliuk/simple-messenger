package com.example.simpleMessenger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chatId;
    private Long senderId;
    private Long recipientId;

    @Convert(converter = com.example.simpleMessenger.config.MessageEncryptionConverter.class)
    private String content;
    private Date timestamp;
    private boolean isRead = false;

    private Date deliveredAt;
    private Date readAt;
    private Date editedAt;
}