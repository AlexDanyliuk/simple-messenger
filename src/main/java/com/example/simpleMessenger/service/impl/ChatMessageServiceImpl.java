package com.example.simpleMessenger.service.impl;

import com.example.simpleMessenger.dto.ChatMessageDto;
import com.example.simpleMessenger.dto.UserListDto;
import com.example.simpleMessenger.entity.ChatMessage;
import com.example.simpleMessenger.repository.ChatMessageRepository;
import com.example.simpleMessenger.service.ChatMessageService;
import com.example.simpleMessenger.service.ChatRoomService;
import com.example.simpleMessenger.service.UserService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository,
                                  ChatRoomService chatRoomService,
                                  SimpMessagingTemplate messagingTemplate,
                                  UserService userService) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomService = chatRoomService;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    @Override
    public void sendMessage(ChatMessageDto dto, Long senderId) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setRecipientId(dto.getRecipientId());
        chatMessage.setContent(dto.getContent());
        chatMessage.setTimestamp(new Date());

        ChatMessage saved = saveChatMessage(chatMessage);

        messagingTemplate.convertAndSend("/topic/chat/" + saved.getChatId(), saved);

        UserListDto recipientDto = userService.getUserListDtoById(dto.getRecipientId());
        recipientDto.setLastMessage(saved.getContent());
        recipientDto.setLastMessageTime(saved.getTimestamp());

        UserListDto senderDto = userService.getUserListDtoById(senderId);
        senderDto.setLastMessage(saved.getContent());
        senderDto.setLastMessageTime(saved.getTimestamp());

        messagingTemplate.convertAndSend("/topic/conversations/" + senderId, recipientDto);
        messagingTemplate.convertAndSend("/topic/conversations/" + dto.getRecipientId(), senderDto);
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
    public List<ChatMessage> findChatMessages(Long senderId, Long recipientId) {
        var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);
        return chatId.map(chatMessageRepository::findByChatIdOrderByTimestampAsc).orElse(new ArrayList<>());
    }
}
