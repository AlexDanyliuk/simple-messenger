package com.example.simpleMessenger.service;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ChatRoomService {

    Optional<String> getChatRoomId(Long senderId, Long recipientId, boolean createNewRoomIfNotExist);
}
