package com.fleetops.triptelemetryservice.storage;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Profile("!dev")
@Slf4j
public class GcsFileStorageService implements FileStorageService {

    @Value("${app.storage.gcs.bucket-name}")
    private String bucketName;

    private Storage storage;

    private Storage getStorage() {
        if (storage == null) {
            storage = StorageOptions.getDefaultInstance().getService();
        }
        return storage;
    }

    @Override
    public String upload(MultipartFile file, String folder) {
        try {
            String extension = getExtension(file.getOriginalFilename());
            String objectName = folder + "/" + UUID.randomUUID() + extension;

            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            getStorage().create(blobInfo, file.getBytes());
            log.info("File uploaded to GCS: gs://{}/{}", bucketName, objectName);

            return objectName;

        } catch (IOException e) {
            log.error("Failed to upload file to GCS", e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<byte[]> download(String objectName) {
        Blob blob = getStorage().get(BlobId.of(bucketName, objectName));
        if (blob == null || !blob.exists()) {
            throw new RuntimeException("File not found in storage: " + objectName);
        }
        byte[] content = blob.getContent();
        MediaType mediaType = blob.getContentType() != null
                ? MediaType.parseMediaType(blob.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(content);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}