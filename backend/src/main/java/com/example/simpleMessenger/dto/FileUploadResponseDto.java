package com.example.simpleMessenger.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResponseDto {
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
}
