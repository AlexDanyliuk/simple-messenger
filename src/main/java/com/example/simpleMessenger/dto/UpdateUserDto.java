package com.example.simpleMessenger.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {
    private String fullName;
    private String avatarUrl;
    private String username;
}
