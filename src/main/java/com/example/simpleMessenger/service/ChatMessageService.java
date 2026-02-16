package com.example.simpleMessenger.service;

import com.example.simpleMessenger.entity.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ChatMessageService {

    ChatMessage saveChatMessage(ChatMessage chatMessage);

    List<ChatMessage> findChatMessages(Long senderId, Long recipientId);
}
