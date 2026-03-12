package com.example.simpleMessenger.controller;

import com.example.simpleMessenger.dto.ChatMessageDto;
import com.example.simpleMessenger.dto.EditMessageDto;
import com.example.simpleMessenger.dto.ReadReceiptDto;
import com.example.simpleMessenger.dto.TypingDto;
import com.example.simpleMessenger.entity.ChatMessage;
import com.example.simpleMessenger.security.CustomUserDetails;
import com.example.simpleMessenger.service.ChatMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageController(ChatMessageService chatMessageService,
                                 SimpMessagingTemplate messagingTemplate) {
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    public void sendChatMessage(@Payload ChatMessageDto dto, Principal principal) {
        CustomUserDetails userDetails =
                (CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Long senderId = userDetails.user().getId();
        chatMessageService.sendMessage(dto, senderId);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingDto dto, Principal principal) {
        CustomUserDetails userDetails =
                (CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        dto.setSenderId(userDetails.user().getId());
        String chatId = dto.getSenderId() < dto.getRecipientId()
                ? dto.getSenderId() + "_" + dto.getRecipientId()
                : dto.getRecipientId() + "_" + dto.getSenderId();
        messagingTemplate.convertAndSend("/topic/typing/" + chatId, dto);
        messagingTemplate.convertAndSend(
            "/topic/conversations/" + dto.getRecipientId(),
            (Object) Map.of("id", dto.getSenderId(), "typing", dto.isTyping())
        );
    }

    /**
     * WebSocket handler: client sends {"senderId": X} to /app/chat.read
     * meaning "I (current user / recipientId) have read messages sent by senderId=X".
     */
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload ReadReceiptDto dto, Principal principal) {
        CustomUserDetails userDetails =
                (CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Long recipientId = userDetails.user().getId();
        chatMessageService.markAsRead(dto.getSenderId(), recipientId);
    }

    @MessageMapping("/chat.edit")
    public void editMessage(@Payload EditMessageDto dto, Principal principal) {
        CustomUserDetails userDetails =
                (CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Long requesterId = userDetails.user().getId();
        chatMessageService.editMessage(dto.getMessageId(), dto.getContent(), requesterId);
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> findChatMessages(@PathVariable Long senderId,
                                                              @PathVariable Long recipientId) {
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, recipientId));
    }
}