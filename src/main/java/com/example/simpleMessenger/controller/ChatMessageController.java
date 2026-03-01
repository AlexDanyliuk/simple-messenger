package com.example.simpleMessenger.controller;

import com.example.simpleMessenger.dto.ChatMessageDto;
import com.example.simpleMessenger.entity.ChatMessage;
import com.example.simpleMessenger.security.CustomUserDetails;
import com.example.simpleMessenger.service.ChatMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @MessageMapping("/chat")
    public void sendChatMessage(@Payload ChatMessageDto dto, Principal principal) {
        CustomUserDetails userDetails =
                (CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Long senderId = userDetails.user().getId();
        chatMessageService.sendMessage(dto, senderId);
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> findChatMessages(@PathVariable Long senderId,
                                                              @PathVariable Long recipientId) {
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, recipientId));
    }

    @PostMapping("/messages/{senderId}/{recipientId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long senderId,
                                           @PathVariable Long recipientId) {
        chatMessageService.markAsRead(senderId, recipientId);
        return ResponseEntity.ok().build();
    }
}