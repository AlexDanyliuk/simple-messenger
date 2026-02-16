package com.example.simpleMessenger.controller;


import com.example.simpleMessenger.dto.ChatMessageDto;
import com.example.simpleMessenger.entity.ChatMessage;
import com.example.simpleMessenger.entity.ChatNotification;
import com.example.simpleMessenger.security.CustomUserDetails;
import com.example.simpleMessenger.service.ChatMessageService;
import com.example.simpleMessenger.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.Date;
import java.util.List;

@Controller
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatMessageController(ChatMessageService chatMessageService, UserService userService, SimpMessagingTemplate simpMessagingTemplate) {
        this.chatMessageService = chatMessageService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/chat")
    public void sendChatMessage(@Payload ChatMessageDto dto, Principal principal){

        CustomUserDetails userDetails =
                (CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        Long senderId = userDetails.user().getId(); // ← ось так

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setRecipientId(dto.getRecipientId());
        chatMessage.setContent(dto.getContent());
        chatMessage.setTimestamp(new Date());

        ChatMessage savedMessage = chatMessageService.saveChatMessage(chatMessage);

        simpMessagingTemplate.convertAndSend(
                "/topic/chat/" + savedMessage.getChatId(),
                savedMessage
        );

    }

    @GetMapping ("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> findChatMessages(@PathVariable Long senderId, @PathVariable Long recipientId){
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, recipientId));
    }
}
