package com.backend.helmaaibackend.service.impl;

import com.backend.helmaaibackend.domain.AuditType;
import com.backend.helmaaibackend.domain.Role;
import com.backend.helmaaibackend.domain.UserAccount;
import com.backend.helmaaibackend.dto.*;
import com.backend.helmaaibackend.exception.BadRequestException;
import com.backend.helmaaibackend.repository.UserAccountRepository;
import com.backend.helmaaibackend.security.JwtService;
import com.backend.helmaaibackend.service.AuditLogService;
import com.backend.helmaaibackend.service.StorageService;
import com.backend.helmaaibackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final StorageService storageService; // <--- yeni

    @Override
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Whitelist: only these roles are allowed
        var allowed = Set.of(Role.ELDER, Role.FAMILY, Role.CAREGIVER);

        List<Role> requested = req.getRoles();
        boolean hasDisallowedRole = requested.stream().anyMatch(r -> !allowed.contains(r));
        if (hasDisallowedRole) {
            throw new BadRequestException("Only ELDER, FAMILY, CAREGIVER roles are allowed on public registration");
        }

        boolean active = req.getActive() == null ? true : req.getActive();

        UserAccount entity = UserAccount.builder()
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .roles(requested)
                .active(active)
                .fullName(req.getFullName())
                .email(req.getEmail())
                .locale(defaultIfBlank(req.getLocale(), "tr-TR"))
                .timeZone(defaultIfBlank(req.getTimeZone(), "Europe/Istanbul"))
                .sttLang(defaultIfBlank(req.getSttLang(), "tr-TR"))
                .ttsVoice(defaultIfBlank(req.getTtsVoice(), "tr-TR-Standard-A"))
                .emergencyContacts(req.getEmergencyContacts())
                .build();

        entity = userRepo.save(entity);

        UserView view = toView(entity);

        String token = jwtService.generate(
                entity.getId(),
                Map.of(
                        "roles", entity.getRoles(),
                        "email", entity.getEmail()
                )
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(view)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        var userOpt = userRepo.findByEmail(req.getEmail());

        if (userOpt.isEmpty()) {
            auditLogService.log(AuditType.LOGIN_FAILED, null, req.getEmail(), null, null, "email not found");
            throw new IllegalArgumentException("Invalid credentials");
        }

        UserAccount user = userOpt.get();

        if (user.getDeletedAt() != null) {
            auditLogService.log(AuditType.LOGIN_FAILED, user.getId(), user.getEmail(), null, null, "user deleted");
            throw new IllegalStateException("User is deleted");
        }
        if (!user.isActive()) {
            auditLogService.log(AuditType.LOGIN_FAILED, user.getId(), user.getEmail(), null, null, "user inactive");
            throw new IllegalStateException("User is not active");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            auditLogService.log(AuditType.LOGIN_FAILED, user.getId(), user.getEmail(), null, null, "wrong password");
            throw new IllegalArgumentException("Invalid credentials");
        }

        user.setLastLoginAt(Instant.now());
        userRepo.save(user); // updatedAt will be set automatically

        auditLogService.log(AuditType.LOGIN_SUCCESS, user.getId(), user.getEmail(), null, null, "login ok");

        UserView view = toView(user);

        String token = jwtService.generate(
                user.getId(),
                Map.of(
                        "roles", user.getRoles(),
                        "email", user.getEmail()
                )
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(view)
                .build();
    }

    @Override
    public UserView toView(String userId) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return toView(user);
    }

    @Override
    public UserView updateProfile(String userId, UpdateProfileRequest request) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getLocale() != null && !request.getLocale().isBlank()) {
            user.setLocale(request.getLocale());
        }
        if (request.getTimeZone() != null && !request.getTimeZone().isBlank()) {
            user.setTimeZone(request.getTimeZone());
        }
        if (request.getSttLang() != null && !request.getSttLang().isBlank()) {
            user.setSttLang(request.getSttLang());
        }
        if (request.getTtsVoice() != null && !request.getTtsVoice().isBlank()) {
            user.setTtsVoice(request.getTtsVoice());
        }

        userRepo.save(user); // updatedAt will be updated by auditing

        return toView(user);
    }

    @Override
    public void updatePassword(String userId, UpdatePasswordRequest request) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Old password is not correct");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user); // updatedAt will be updated by auditing
    }

    @Override
    public UserView updateEmergencyContacts(String userId, UpdateEmergencyContactsRequest request) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        user.setEmergencyContacts(request.getEmergencyContacts());
        userRepo.save(user); // updatedAt will be updated by auditing

        return toView(user);
    }

    // deactivate
    @Override
    public void deactivateAccount(String userId) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        user.setActive(false);
        user.setDeletedAt(Instant.now());
        userRepo.save(user);

        auditLogService.log(AuditType.USER_DEACTIVATE_SELF, user.getId(), user.getEmail(), user.getId(), user.getEmail(), "self-deactivate");
    }

    /**
     * Profil fotoğrafını güncelle (Azure Storage upload + eski foto silme).
     */
    @Override
    public UserView updateProfilePhoto(String userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Profile photo file is empty");
        }

        // Basit mime-type kontrolü
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equalsIgnoreCase("image/jpeg")
                        || contentType.equalsIgnoreCase("image/png"))) {
            throw new BadRequestException("Only JPEG and PNG images are allowed");
        }

        // Boyut sınırı (örnek: 5 MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size must be <= 5MB");
        }

        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (user.getDeletedAt() != null) {
            throw new IllegalStateException("User is deleted");
        }
        if (!user.isActive()) {
            throw new IllegalStateException("User is not active");
        }

        // Eski profil fotoğrafı varsa Azure'dan sil
        if (user.getProfilePhotoBlobName() != null) {
            storageService.deleteProfilePhoto(user.getProfilePhotoBlobName());
        }

        // Yeni profil fotoğrafını upload et
        String url = storageService.uploadProfilePhoto(userId, file);

        // URL'den blobName'i çıkar (container içi path)
        String blobName = extractBlobNameFromUrl(url);

        user.setProfilePhotoUrl(url);
        user.setProfilePhotoBlobName(blobName);

        userRepo.save(user);

        return toView(user);
    }

    /**
     * Profil fotoğrafını kaldır (Azure + DB).
     */
    @Override
    public UserView deleteProfilePhoto(String userId) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (user.getDeletedAt() != null) {
            throw new IllegalStateException("User is deleted");
        }
        if (!user.isActive()) {
            throw new IllegalStateException("User is not active");
        }

        if (user.getProfilePhotoBlobName() != null) {
            storageService.deleteProfilePhoto(user.getProfilePhotoBlobName());
        }

        user.setProfilePhotoUrl(null);
        user.setProfilePhotoBlobName(null);

        userRepo.save(user);

        return toView(user);
    }

    private UserView toView(UserAccount u) {
        return UserView.builder()
                .id(u.getId())
                .roles(u.getRoles())
                .active(u.isActive())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .lastLoginAt(u.getLastLoginAt())
                .locale(u.getLocale())
                .timeZone(u.getTimeZone())
                .sttLang(u.getSttLang())
                .ttsVoice(u.getTtsVoice())
                .emergencyContacts(u.getEmergencyContacts())
                .profilePhotoUrl(u.getProfilePhotoUrl())
                .build();
    }

    private String defaultIfBlank(String val, String def) {
        return (val == null || val.isBlank()) ? def : val;
    }

    /**
     * https://account.blob.core.windows.net/container/profile-photos/userId/uuid.png
     * -> profile-photos/userId/uuid.png
     */
    private String extractBlobNameFromUrl(String url) {
        if (url == null) {
            return null;
        }
        int idx = url.indexOf(".blob.core.windows.net/");
        if (idx == -1) {
            return null;
        }
        String afterHost = url.substring(idx + ".blob.core.windows.net/".length());
        // afterHost = "container/profile-photos/userId/uuid.png"
        int firstSlash = afterHost.indexOf('/');
        if (firstSlash == -1) {
            return null;
        }
        return afterHost.substring(firstSlash + 1); // "profile-photos/userId/uuid.png"
    }
}
