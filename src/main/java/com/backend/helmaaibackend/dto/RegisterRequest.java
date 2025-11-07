package com.backend.helmaaibackend.dto;

import com.backend.helmaaibackend.domain.EmergencyContact;
import com.backend.helmaaibackend.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 8, max = 128)
    private String password; // plain text at register time only

    @NotBlank
    @Size(min = 3, max = 100)
    private String fullName;

    @Email
    @NotBlank
    private String email;

    // REQUIRED: Only ELDER/FAMILY/CAREGIVER roles are accepted for public registration (validated in service layer)
    @NotNull(message = "At least one role must be provided")
    @Size(min = 1, message = "At least one role must be provided")
    private List<Role> roles;

    // If null, defaults to true in service layer (starts as active)
    private Boolean active;

    private String locale;
    private String timeZone;
    private String sttLang;
    private String ttsVoice;

    private List<EmergencyContact> emergencyContacts;
}
