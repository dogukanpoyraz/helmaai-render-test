package com.backend.helmaaibackend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 3, max = 100)
    private String fullName;

    // Language/location settings are optional; if null, we won't change them
    private String locale;          // e.g.: "tr-TR"
    private String timeZone;        // e.g.: "Europe/Istanbul"
    private String sttLang;         // e.g.: "tr-TR"
    private String ttsVoice;        // e.g.: "tr-TR-Standard-A"
}
