package com.example.simpleMessenger.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserListDto {

    private Long id;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String status;
    private String lastMessage;
    private java.util.Date lastMessageTime;
    private int unreadCount;
}