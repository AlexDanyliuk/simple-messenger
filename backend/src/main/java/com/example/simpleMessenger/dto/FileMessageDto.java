package com.example.simpleMessenger.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMessageDto {
    private Long recipientId;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
}
