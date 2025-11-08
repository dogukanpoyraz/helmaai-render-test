package com.backend.helmaaibackend.dto;

import com.backend.helmaaibackend.domain.EmergencyStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmergencyTriggerResponse {
    private String eventId;
    private EmergencyStatus status;
    private String message;
}
