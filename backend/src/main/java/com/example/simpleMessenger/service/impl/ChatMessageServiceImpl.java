package com.example.simpleMessenger.service.impl;

import com.example.simpleMessenger.dto.ChatMessageDto;
import com.example.simpleMessenger.dto.MessageStatusUpdateDto;
import com.example.simpleMessenger.dto.UserListDto;
import com.example.simpleMessenger.dto.UserProfileDto;
import com.example.simpleMessenger.entity.ChatMessage;
import com.example.simpleMessenger.repository.ChatMessageRepository;
import com.example.simpleMessenger.service.ChatMessageService;
import com.example.simpleMessenger.service.ChatRoomService;
import com.example.simpleMessenger.service.UserService;
import jakarta.transaction.Transactional;
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
        chatMessage.setRead(false);

        ChatMessage saved = saveChatMessage(chatMessage);

        messagingTemplate.convertAndSend("/topic/chat/" + saved.getChatId(), saved);

        // Mark delivered immediately if recipient is currently online
        UserProfileDto recipientProfile = userService.getUserById(dto.getRecipientId());
        if ("ONLINE".equals(recipientProfile.getStatus())) {
            saved.setDeliveredAt(new Date());
            chatMessageRepository.save(saved);
            messagingTemplate.convertAndSend(
                    "/topic/message-status/" + saved.getChatId(),
                    new MessageStatusUpdateDto(saved.getSenderId(), "DELIVERED", saved.getChatId())
            );
        }

        int unreadForRecipient = chatMessageRepository.countBySenderIdAndRecipientIdAndIsRead(
                senderId, dto.getRecipientId(), false
        );

        UserListDto recipientDto = userService.getUserListDtoById(dto.getRecipientId());
        recipientDto.setLastMessage(saved.getContent());
        recipientDto.setLastMessageTime(saved.getTimestamp());
        recipientDto.setUnreadCount(0);

        UserListDto senderDto = userService.getUserListDtoById(senderId);
        senderDto.setLastMessage(saved.getContent());
        senderDto.setLastMessageTime(saved.getTimestamp());
        senderDto.setUnreadCount(unreadForRecipient);

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

    @Override
    @Transactional
    public void markAsRead(Long senderId, Long recipientId) {
        Date now = new Date();
        List<ChatMessage> unread = chatMessageRepository.findBySenderIdAndRecipientIdAndIsRead(
                senderId, recipientId, false
        );
        unread.forEach(msg -> {
            msg.setRead(true);
            if (msg.getDeliveredAt() == null) msg.setDeliveredAt(now);
            msg.setReadAt(now);
        });
        chatMessageRepository.saveAll(unread);

        // Broadcast read status so sender sees blue ticks in real-time
        if (!unread.isEmpty()) {
            String chatId = unread.get(0).getChatId();
            messagingTemplate.convertAndSend(
                    "/topic/message-status/" + chatId,
                    new MessageStatusUpdateDto(senderId, "READ", chatId)
            );
        }

        UserListDto senderDto = userService.getUserListDtoById(senderId);
        senderDto.setUnreadCount(0);

        chatMessageRepository
                .findTopBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampDesc(
                        senderId, recipientId, recipientId, senderId
                )
                .ifPresent(msg -> {
                    senderDto.setLastMessage(msg.getContent());
                    senderDto.setLastMessageTime(msg.getTimestamp());
                });

        messagingTemplate.convertAndSend("/topic/conversations/" + recipientId, senderDto);
    }

    @Override
    @Transactional
    public void editMessage(Long messageId, String newContent, Long requesterId) {
        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!msg.getSenderId().equals(requesterId)) {
            throw new RuntimeException("Not allowed to edit this message");
        }

        msg.setContent(newContent);
        msg.setEditedAt(new Date());
        chatMessageRepository.save(msg);

        messagingTemplate.convertAndSend(
                "/topic/chat-edit/" + msg.getChatId(),
                msg
        );
    }
}