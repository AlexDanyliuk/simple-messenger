package com.example.simpleMessenger.dto;

public record MessageStatusUpdateDto(
        Long senderId,
        String status,   // "DELIVERED" | "READ"
        String chatId
) {}
