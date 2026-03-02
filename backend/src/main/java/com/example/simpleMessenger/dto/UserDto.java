package com.example.simpleMessenger.dto;

import lombok.Data;

@Data
public class UserDto {
    Long id;
    String username;
    String fullName;
    String email;
    String password;
}
