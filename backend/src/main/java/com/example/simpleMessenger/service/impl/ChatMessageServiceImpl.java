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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        // Set file metadata if present
        if (dto.getFileUrl() != null && !dto.getFileUrl().isBlank()) {
            chatMessage.setFileUrl(dto.getFileUrl());
            chatMessage.setFileName(dto.getFileName());
            chatMessage.setFileType(dto.getFileType());
            chatMessage.setFileSize(dto.getFileSize());
        }

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
        String lastMessage = saved.getContent() != null && !saved.getContent().isBlank() 
                ? saved.getContent() 
                : (saved.getFileName() != null ? "📎 " + saved.getFileName() : "Message");
        recipientDto.setLastMessage(lastMessage);
        recipientDto.setLastMessageTime(saved.getTimestamp());
        recipientDto.setUnreadCount(0);

        UserListDto senderDto = userService.getUserListDtoById(senderId);
        senderDto.setLastMessage(lastMessage);
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
                    String lastMessage = msg.getContent() != null && !msg.getContent().isBlank()
                            ? msg.getContent()
                            : (msg.getFileName() != null ? "📎 " + msg.getFileName() : "Message");
                    senderDto.setLastMessage(lastMessage);
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

    @Override
    @Transactional
    public void toggleReaction(Long messageId, String emoji, Long requesterId) {
        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        boolean participant = msg.getSenderId().equals(requesterId) || msg.getRecipientId().equals(requesterId);
        if (!participant) {
            throw new RuntimeException("Not allowed to react to this message");
        }

        String normalizedEmoji = emoji == null ? "" : emoji.trim();
        if (normalizedEmoji.isBlank() || normalizedEmoji.length() > 8) {
            throw new IllegalArgumentException("Invalid emoji");
        }

        Map<String, List<Long>> reactions = msg.getReactions();
        Set<Long> users = new LinkedHashSet<>(reactions.getOrDefault(normalizedEmoji, List.of()));
        if (!users.add(requesterId)) {
            users.remove(requesterId);
        }

        if (users.isEmpty()) {
            reactions.remove(normalizedEmoji);
        } else {
            reactions.put(normalizedEmoji, new ArrayList<>(users));
        }

        msg.setReactions(reactions);
        ChatMessage saved = chatMessageRepository.save(msg);

        messagingTemplate.convertAndSend(
                "/topic/chat-reaction/" + saved.getChatId(),
                saved
        );
    }

    @Override
    @Transactional
    public void togglePin(Long messageId, boolean pinned, Long requesterId) {
        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Allow both sender and recipient to pin/unpin messages in their chat
        boolean isParticipant = msg.getSenderId().equals(requesterId) || msg.getRecipientId().equals(requesterId);
        if (!isParticipant) {
            throw new RuntimeException("Not allowed to pin/unpin this message");
        }

        msg.setPinned(pinned);
        ChatMessage saved = chatMessageRepository.save(msg);

        messagingTemplate.convertAndSend(
                "/topic/chat-pin/" + saved.getChatId(),
                saved
        );
    }
}