package com.fleetops.triptelemetryservice.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.storage.local-path}")
    private String basePath;

    @Override
    public String upload(MultipartFile file, String folder) {
        try {
            Path targetDir = Path.of(basePath, folder);
            Files.createDirectories(targetDir);
            String extension = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + extension;
            Path targetPath = targetDir.resolve(filename);
            file.transferTo(targetPath);
            log.info("File stored locally: {}", targetPath);
            return "/uploads/" + folder + "/" + filename;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}