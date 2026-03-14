package com.example.simpleMessenger.service;

import org.springframework.web.multipart.MultipartFile;
import com.example.simpleMessenger.dto.FileUploadResponseDto;

import java.io.IOException;

public interface FileStorageService {
    FileUploadResponseDto uploadFile(MultipartFile file) throws IOException;
    void deleteFile(String fileUrl);
}
