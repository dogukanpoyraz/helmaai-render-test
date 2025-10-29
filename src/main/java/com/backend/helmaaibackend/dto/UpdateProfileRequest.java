package com.backend.helmaaibackend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 3, max = 100)
    private String fullName;

    // Dil/konum ayarları opsiyonel; null gelirse değiştirmeyeceğiz
    private String locale;          // örn: "tr-TR"
    private String timeZone;        // örn: "Europe/Istanbul"
    private String sttLang;         // örn: "tr-TR"
    private String ttsVoice;        // örn: "tr-TR-Standard-A"
}
