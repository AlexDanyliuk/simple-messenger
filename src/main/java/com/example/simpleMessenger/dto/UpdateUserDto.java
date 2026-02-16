package com.example.simpleMessenger.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {
    private String fullName;
    private String avatarUrl;

    @Size(min = 3, max = 20)
    private String username;
}
