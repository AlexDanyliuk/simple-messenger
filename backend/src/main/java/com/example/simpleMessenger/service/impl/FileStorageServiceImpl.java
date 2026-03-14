package com.example.simpleMessenger.service.impl;

import com.example.simpleMessenger.dto.FileUploadResponseDto;
import com.example.simpleMessenger.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:8080/files}")
    private String baseUrl;

    @Override
    public FileUploadResponseDto uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        // Validate file size (300MB max) - enforced by Spring config too
        long maxSize = 300 * 1024 * 1024; // 300MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File exceeds maximum size of 300MB");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID() + fileExtension;

        // Save file to disk
        Path filePath = uploadPath.resolve(newFileName);
        Files.write(filePath, file.getBytes());

        // Return response DTO
        return new FileUploadResponseDto(
                baseUrl + "/" + newFileName,
                originalFileName,
                file.getContentType(),
                file.getSize()
        );
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extract filename from URL
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir, fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log but don't fail
            System.err.println("Failed to delete file: " + fileUrl);
        }
    }
}
