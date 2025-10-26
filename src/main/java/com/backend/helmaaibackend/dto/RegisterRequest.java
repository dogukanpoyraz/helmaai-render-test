package com.backend.helmaaibackend.dto;

import com.backend.helmaaibackend.domain.EmergencyContact;
import com.backend.helmaaibackend.domain.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {

    // username GİTTİ

    @NotBlank
    @Size(min=8, max=128)
    private String password; // plain text at register time only

    @NotBlank
    @Size(min=3, max=100)
    private String fullName;

    @Email
    @NotBlank
    private String email;

    private List<Role> roles; // null ise ELDER atayacağız
    private Boolean active;   // null ise true

    private String locale;
    private String timeZone;
    private String sttLang;
    private String ttsVoice;

    // notificationPrefs GİTTİ

    private List<EmergencyContact> emergencyContacts;
}
