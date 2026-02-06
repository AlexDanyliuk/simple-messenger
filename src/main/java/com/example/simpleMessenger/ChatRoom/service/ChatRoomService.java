package com.example.simpleMessenger.ChatRoom.service;

import com.example.simpleMessenger.ChatRoom.entity.ChatRoom;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ChatRoomService {

    Optional<String> getChatRoomId(String senderId, String recipientId, boolean createNewRoomIfNotExist);
}
