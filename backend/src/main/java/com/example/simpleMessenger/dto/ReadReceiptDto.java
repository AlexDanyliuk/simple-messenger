package com.example.simpleMessenger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadReceiptDto {
    /** ID of the user whose messages are being marked read (the original sender) */
    private Long senderId;
}
