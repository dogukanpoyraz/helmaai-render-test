package com.backend.helmaaibackend.service.impl;

import com.backend.helmaaibackend.domain.*;
import com.backend.helmaaibackend.dto.EmergencyTriggerRequest;
import com.backend.helmaaibackend.dto.EmergencyTriggerResponse;
import com.backend.helmaaibackend.exception.BadRequestException;
import com.backend.helmaaibackend.repository.EmergencyEventRepository;
import com.backend.helmaaibackend.service.AuditLogService;
import com.backend.helmaaibackend.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmergencyServiceImpl implements EmergencyService {

    private final EmergencyEventRepository emergencyRepo;
    private final AuditLogService audit;

    // Simple rate-limit: prevent the same user from triggering multiple times within 60 seconds (to prevent accidental double-clicks)
    private static final long TRIGGER_COOLDOWN_SECONDS = 60;

    @Override
    public EmergencyTriggerResponse trigger(UserAccount currentUser, EmergencyTriggerRequest req) {
        if (currentUser.getDeletedAt() != null || !currentUser.isActive()) {
            throw new BadRequestException("Account is not active");
        }

        // Rate-limit check
        Instant cutoff = Instant.now().minusSeconds(TRIGGER_COOLDOWN_SECONDS);
        long recent = emergencyRepo.countByUserIdAndCreatedAtAfter(currentUser.getId(), cutoff);
        if (recent > 0) {
            throw new BadRequestException("Emergency already triggered recently");
        }

        Instant clientTriggeredAt = null;
        if (req.getClientTriggeredAtIso() != null && !req.getClientTriggeredAtIso().isBlank()) {
            try {
                clientTriggeredAt = Instant.parse(req.getClientTriggeredAtIso());
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid clientTriggeredAtIso format");
            }
        }

        List<EmergencyContact> snapshot = currentUser.getEmergencyContacts(); // can be null

        EmergencyEvent event = EmergencyEvent.builder()
                .userId(currentUser.getId())
                .userEmail(currentUser.getEmail())
                .userFullName(currentUser.getFullName())
                .status(EmergencyStatus.OPEN)
                .lat(req.getLat())
                .lon(req.getLon())
                .accuracyMeters(req.getAccuracyMeters())
                .note(req.getNote())
                .triggeredAt(clientTriggeredAt != null ? clientTriggeredAt : Instant.now())
                .contactsSnapshot(snapshot)
                .build();

        event = emergencyRepo.save(event);

        // Audit log
        audit.log(AuditType.EMERGENCY_TRIGGER, currentUser.getId(), currentUser.getEmail(),
                currentUser.getId(), currentUser.getEmail(),
                "eventId=" + event.getId());

        // Notification stub: log audit for each contact (SMS/Push/Call will be integrated in the future)
        if (snapshot != null && !snapshot.isEmpty()) {
            for (EmergencyContact c : snapshot) {
                String details = "notify -> " + c.getName() + " (" + c.getRelation() + ") " + c.getPhone();
                audit.log(AuditType.EMERGENCY_CONTACT_NOTIFY, currentUser.getId(), currentUser.getEmail(),
                        currentUser.getId(), currentUser.getEmail(), details);
            }
        } else {
            audit.log(AuditType.ERROR, currentUser.getId(), currentUser.getEmail(),
                    currentUser.getId(), currentUser.getEmail(), "No emergency contacts on user");
        }

        return EmergencyTriggerResponse.builder()
                .eventId(event.getId())
                .status(event.getStatus())
                .message("Emergency request received")
                .build();
    }
}
