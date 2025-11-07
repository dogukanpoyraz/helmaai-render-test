package com.backend.helmaaibackend.controller;

import com.backend.helmaaibackend.domain.UserAccount;
import com.backend.helmaaibackend.dto.*;
import com.backend.helmaaibackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile", description = "Active user's own account")
@RestController
@RequestMapping("/api/profile")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    private String currentUserIdOrThrow() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserAccount user)) {
            throw new IllegalStateException("Not authenticated");
        }
        return user.getId();
    }

    @Operation(
            summary = "Get active user's profile",
            description = "Resolves user from Bearer JWT token in header and returns UserView."
    )
    @GetMapping
    public ResponseEntity<UserView> me() {
        String userId = currentUserIdOrThrow();
        return ResponseEntity.ok(userService.toView(userId));
    }

    @Operation(
            summary = "Update profile information",
            description = "Updates fullName, locale, timeZone, sttLang, ttsVoice fields."
    )
    @PutMapping
    public ResponseEntity<UserView> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        String userId = currentUserIdOrThrow();
        UserView updated = userService.updateProfile(userId, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Change password",
            description = "Updates with new password when correct old password is provided."
    )
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        String userId = currentUserIdOrThrow();
        userService.updatePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Update emergency contacts",
            description = "Completely replaces the emergencyContacts list."
    )
    @PutMapping("/emergency-contacts")
    public ResponseEntity<UserView> updateEmergencyContacts(
            @Valid @RequestBody UpdateEmergencyContactsRequest request
    ) {
        String userId = currentUserIdOrThrow();
        UserView updated = userService.updateEmergencyContacts(userId, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Deactivate account (self-deactivate)",
            description = "Set active=false and deletedAt. After this process, the user cannot log in and will not appear in the admin lists."
    )
    @PutMapping("/deactivate")
    public ResponseEntity<Void> deactivate() {
        String userId = currentUserIdOrThrow();
        userService.deactivateAccount(userId);
        return ResponseEntity.noContent().build();
    }
}
