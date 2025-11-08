package com.backend.helmaaibackend.controller;

import com.backend.helmaaibackend.domain.UserAccount;
import com.backend.helmaaibackend.dto.EmergencyTriggerRequest;
import com.backend.helmaaibackend.dto.EmergencyTriggerResponse;
import com.backend.helmaaibackend.service.EmergencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Emergency", description = "Emergency triggering")
@RestController
@RequestMapping("/api/emergency")
public class EmergencyController {

    private final EmergencyService emergencyService;

    public EmergencyController(EmergencyService emergencyService) {
        this.emergencyService = emergencyService;
    }

    private UserAccount currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (UserAccount) auth.getPrincipal();
    }

    @Operation(summary = "Trigger emergency", description = "Called by mobile button. Creates an event record and starts the notification process.")
    @PostMapping("/trigger")
    public ResponseEntity<EmergencyTriggerResponse> trigger(@Valid @RequestBody EmergencyTriggerRequest req) {
        var me = currentUser();
        EmergencyTriggerResponse res = emergencyService.trigger(me, req);
        // Could return 201; here 200/OK is practical, or you can set 201 Created + Location header if desired.
        return ResponseEntity.ok(res);
    }
}
