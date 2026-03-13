package com.example.simpleMessenger.dto;

public record StatusUpdateDto(
        Long userId,
        String username,
        String status,
        java.util.Date lastSeenAt
) {}
