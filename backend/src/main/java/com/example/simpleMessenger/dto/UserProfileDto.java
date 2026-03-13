package com.example.simpleMessenger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor

@Getter
@Setter
public class UserProfileDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String status;
    private java.util.Date lastSeenAt;
    private String theme;
    private String language;
}
