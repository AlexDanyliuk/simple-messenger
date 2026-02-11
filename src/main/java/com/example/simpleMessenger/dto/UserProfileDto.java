package com.example.simpleMessenger.dto;

import com.example.simpleMessenger.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor

@Getter
@Setter
public class UserProfileDto {
    private String username;
    private String fullName;
    private String email;
    private String avatarUrl;
}
