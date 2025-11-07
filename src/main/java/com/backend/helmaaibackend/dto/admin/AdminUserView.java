package com.backend.helmaaibackend.dto.admin;

import com.backend.helmaaibackend.domain.EmergencyContact;
import com.backend.helmaaibackend.domain.Role;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class AdminUserView {
    private String id;
    private List<Role> roles;
    private boolean active;
    private String fullName;
    private String email;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    private String locale;
    private String timeZone;
    private String sttLang;
    private String ttsVoice;

    private List<EmergencyContact> emergencyContacts;
}
