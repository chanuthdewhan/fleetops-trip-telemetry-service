package com.fleetops.triptelemetryservice.storage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String upload(MultipartFile file, String folder);
    ResponseEntity<byte[]> download(String pathOrObjectName);
}