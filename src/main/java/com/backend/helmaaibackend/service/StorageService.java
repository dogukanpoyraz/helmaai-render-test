// service/StorageService.java
package com.backend.helmaaibackend.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /**
     * Profil fotoğrafını Azure Blob’a upload eder.
     * @return Blob URL (profilPhotoUrl)
     */
    String uploadProfilePhoto(String userId, MultipartFile file);

    /**
     * Mevcut blob’ı silmek için.
     */
    void deleteProfilePhoto(String blobName);
}
