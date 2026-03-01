package com.example.simpleMessenger.repository;

import com.example.simpleMessenger.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatIdOrderByTimestampAsc(String chatId);

    Optional<ChatMessage> findTopBySenderIdAndRecipientIdOrderByTimestampDesc(Long senderId, Long recipientId);

    List<ChatMessage> findBySenderIdAndRecipientIdOrderByTimestampAsc(Long senderId, Long recipientId);

    List<ChatMessage> findBySenderIdOrRecipientId(Long senderId, Long recipientId);

    Optional<ChatMessage> findTopBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampDesc(
            Long senderId1, Long recipientId1,
            Long senderId2, Long recipientId2
    );

    int countBySenderIdAndRecipientIdAndIsRead(Long senderId, Long recipientId, boolean isRead);

    List<ChatMessage> findBySenderIdAndRecipientIdAndIsRead(Long senderId, Long recipientId, boolean isRead);
}