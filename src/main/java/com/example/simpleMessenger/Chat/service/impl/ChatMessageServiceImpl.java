package com.example.simpleMessenger.Chat.service.impl;

import com.example.simpleMessenger.Chat.entity.ChatMessage;
import com.example.simpleMessenger.Chat.repository.ChatMessageRepository;
import com.example.simpleMessenger.Chat.service.ChatMessageService;
import com.example.simpleMessenger.ChatRoom.service.ChatRoomService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;

    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository, ChatRoomService chatRoomService) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomService = chatRoomService;
    }


    @Override
    public ChatMessage saveChatMessage(ChatMessage chatMessage) {

        var chatId = chatRoomService.getChatRoomId(
                chatMessage.getSenderId(),
                chatMessage.getRecipientId(),
                true
        ).orElseThrow();

        chatMessage.setChatId(chatId);
        chatMessageRepository.save(chatMessage);
        return chatMessage;
    }


    @Override
    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);;
        return chatId.map(chatMessageRepository::findByChatId).orElse(new ArrayList<>());
    }
}
