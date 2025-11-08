package com.backend.helmaaibackend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** Location is optional; not sent if device permission is not granted. */
@Data
public class EmergencyTriggerRequest {

    private Double lat;
    private Double lon;
    private Double accuracyMeters;

    @Size(max = 500)
    private String note;

    /** Device trigger time; if not sent, backend uses createdAt */
    private String clientTriggeredAtIso; // ISO-8601 string, optional
}
