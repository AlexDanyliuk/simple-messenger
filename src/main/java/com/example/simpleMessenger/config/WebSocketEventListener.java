package com.example.simpleMessenger.config;

import com.example.simpleMessenger.dto.StatusUpdateDto;
import com.example.simpleMessenger.entity.User;
import com.example.simpleMessenger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;


    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Principal principal = accessor.getUser();

        if (principal == null) return;


        String email = principal.getName();

        User user = userService.setUserOnline(email);

        messagingTemplate.convertAndSend(
                "/topic/status",
                new StatusUpdateDto(
                        user.getId(),
                        user.getUsername(),
                        user.getStatus().name()
                )
        );

        System.out.println("User ONLINE: " + user.getUsername());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

        Principal principal = event.getUser();

        if (principal == null) return;

        String email = principal.getName();

        User user = userService.setUserOffline(email);

        messagingTemplate.convertAndSend(
                "/topic/status",
                new StatusUpdateDto(
                        user.getId(),
                        user.getUsername(),
                        user.getStatus().name()
                )
        );

        System.out.println("User OFFLINE: " + user.getUsername());
    }
}