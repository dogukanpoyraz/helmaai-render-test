package com.backend.helmaaibackend.controller;

import com.backend.helmaaibackend.domain.UserAccount;
import com.backend.helmaaibackend.dto.UpdateEmergencyContactsRequest;
import com.backend.helmaaibackend.dto.UpdatePasswordRequest;
import com.backend.helmaaibackend.dto.UpdateProfileRequest;
import com.backend.helmaaibackend.dto.UserView;
import com.backend.helmaaibackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile", description = "Aktif kullanıcının hesap yönetimi")
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    /**
     * SecurityContext'ten giriş yapan kullanıcının ID'sini alır.
     * JwtAuthFilter zaten SecurityContext'e UserAccount koyuyor.
     */
    private String currentUserIdOrThrow() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserAccount user)) {
            throw new IllegalStateException("Not authenticated");
        }
        return user.getId();
    }

    @Operation(
            summary = "Aktif kullanıcının profilini getir",
            description = "Header'daki Bearer JWT token'dan kullanıcıyı çözer ve UserView döner."
    )
    @GetMapping
    public ResponseEntity<UserView> me() {
        String userId = currentUserIdOrThrow();
        UserView view = userService.toView(userId);
        return ResponseEntity.ok(view);
    }

    @Operation(
            summary = "Profil bilgilerini güncelle",
            description = "fullName, locale, timeZone, sttLang, ttsVoice alanlarını günceller. Null olan alanlar değişmez."
    )
    @PutMapping
    public ResponseEntity<UserView> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        String userId = currentUserIdOrThrow();
        UserView updated = userService.updateProfile(userId, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Şifre değiştir",
            description = "Doğru eski şifre verildiğinde yeni şifre ile günceller. Başarılıysa 204 döner."
    )
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        String userId = currentUserIdOrThrow();
        userService.updatePassword(userId, request);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(
            summary = "Acil durum kişilerini güncelle",
            description = "emergencyContacts listesini tamamen değiştirir ve güncel profili döner."
    )
    @PutMapping("/emergency-contacts")
    public ResponseEntity<UserView> updateEmergencyContacts(
            @Valid @RequestBody UpdateEmergencyContactsRequest request
    ) {
        String userId = currentUserIdOrThrow();
        UserView updated = userService.updateEmergencyContacts(userId, request);
        return ResponseEntity.ok(updated);
    }
}
