package com.example.simpleMessenger.controller;


import com.example.simpleMessenger.entity.ChatMessage;
import com.example.simpleMessenger.entity.ChatNotification;
import com.example.simpleMessenger.service.ChatMessageService;
import com.example.simpleMessenger.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    public void sendChatMessage(@Payload ChatMessage chatMessage){
        ChatMessage savedMessage = chatMessageService.saveChatMessage(chatMessage);
        simpMessagingTemplate.convertAndSendToUser(savedMessage.getRecipientId(), "/queue/messages",
                new ChatNotification(
                        savedMessage.getId(),
                        savedMessage.getSenderId(),
                        savedMessage.getRecipientId(),
                        savedMessage.getContent()
                )
        );
    }

    @GetMapping ("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> findChatMessages(@PathVariable String senderId, @PathVariable String recipientId){
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, recipientId));
    }
}
