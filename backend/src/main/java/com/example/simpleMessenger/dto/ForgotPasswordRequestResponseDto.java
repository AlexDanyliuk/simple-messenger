package com.example.simpleMessenger.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequestResponseDto {
    private String message;

    public ForgotPasswordRequestResponseDto(String message) {
        this.message = message;
    }
}
