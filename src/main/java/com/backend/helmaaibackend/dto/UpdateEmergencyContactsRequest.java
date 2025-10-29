package com.backend.helmaaibackend.dto;

import com.backend.helmaaibackend.domain.EmergencyContact;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateEmergencyContactsRequest {

    @NotNull
    private List<EmergencyContact> emergencyContacts;
}
