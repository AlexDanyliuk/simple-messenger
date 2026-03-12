package com.example.simpleMessenger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditMessageDto {
    private Long messageId;
    private String content;
}
