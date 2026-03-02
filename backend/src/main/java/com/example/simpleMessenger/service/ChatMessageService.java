package com.example.simpleMessenger.service;

import com.example.simpleMessenger.dto.ChatMessageDto;
import com.example.simpleMessenger.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    void sendMessage(ChatMessageDto dto, Long senderId);
    ChatMessage saveChatMessage(ChatMessage chatMessage);
    List<ChatMessage> findChatMessages(Long senderId, Long recipientId);
    void markAsRead(Long senderId, Long recipientId);
}