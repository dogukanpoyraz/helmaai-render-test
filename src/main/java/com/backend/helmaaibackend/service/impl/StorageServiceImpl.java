package com.backend.helmaaibackend.service.impl;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.backend.helmaaibackend.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@Service
public class StorageServiceImpl implements StorageService {

    private final BlobContainerClient profilePhotoContainerClient;

    public StorageServiceImpl(
            @Value("${AZURE_STORAGE_CONNECTION_STRING:}") String connectionString,
            @Value("${helma.storage.profile-container-name}") String profileContainerName
    ) {
        if (connectionString == null || connectionString.isBlank()) {
            // Burada RuntimeException fırlatmak daha iyi,
            // çünkü storage olmadan app'in ayağa kalkması anlamsız.
            throw new IllegalStateException("AZURE_STORAGE_CONNECTION_STRING is not configured");
        }

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        this.profilePhotoContainerClient = blobServiceClient.getBlobContainerClient(profileContainerName);

        if (!this.profilePhotoContainerClient.exists()) {
            this.profilePhotoContainerClient.create();
            // NOT: İstersen burada container public access ayarı yapabilirsin.
            // Varsayılan: private container (sadece SAS veya backend üzerinden erişim).
        }
    }

    @Override
    public String uploadProfilePhoto(String userId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);

        // Container içindeki path (folder gibi davranıyor)
        String blobName = "profile-photos/" + userId + "/" + UUID.randomUUID() + extension;
        BlobClient blobClient = profilePhotoContainerClient.getBlobClient(blobName);

        try {
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            // Content-Type doğru olsun (image/jpeg, image/png vs.)
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType());
            blobClient.setHttpHeaders(headers);

            // Burada dönen URL, UserServiceImpl tarafında
            // extractBlobNameFromUrl ile parse edilip blobName elde ediliyor.
            return blobClient.getBlobUrl();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile photo", e);
        }
    }

    @Override
    public void deleteProfilePhoto(String blobName) {
        if (blobName == null || blobName.isBlank()) {
            return;
        }
        BlobClient blobClient = profilePhotoContainerClient.getBlobClient(blobName);
        if (blobClient.exists()) {
            blobClient.delete();
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int idx = filename.lastIndexOf('.');
        if (idx == -1) {
            return "";
        }
        String ext = filename.substring(idx).toLowerCase(Locale.ROOT);
        return ext;
    }
}
