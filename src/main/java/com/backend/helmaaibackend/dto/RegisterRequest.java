package com.backend.helmaaibackend.dto;

import com.backend.helmaaibackend.domain.EmergencyContact;
import com.backend.helmaaibackend.domain.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min=8, max=128)
    private String password; // plain text at register time only

    @NotBlank
    @Size(min=3, max=100)
    private String fullName;

    @Email
    @NotBlank
    private String email;

    private List<Role> roles; // null will assign ELDER
    private Boolean active;   // null will assign true

    private String locale;
    private String timeZone;
    private String sttLang;
    private String ttsVoice;

    private List<EmergencyContact> emergencyContacts;
}
