package com.backend.helmaaibackend.controller.admin;

import com.backend.helmaaibackend.domain.AuditType;
import com.backend.helmaaibackend.domain.UserAccount;
import com.backend.helmaaibackend.dto.admin.AuditLogView;
import com.backend.helmaaibackend.dto.admin.AnnouncementRequest;
import com.backend.helmaaibackend.service.AuditLogService;
import com.backend.helmaaibackend.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Ops", description = "Log and Announcement operations (ADMIN only)")
@RestController
@RequestMapping("/api/admin")
public class AdminOpsController {

    private final AuditLogService audit;
    private final AnnouncementService announcementService;

    public AdminOpsController(AuditLogService audit, AnnouncementService announcementService) {
        this.audit = audit;
        this.announcementService = announcementService;
    }

    private UserAccount actor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (UserAccount) auth.getPrincipal();
    }

    @Operation(summary = "List logs (paginated)")
    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLogView>> logs(
            @RequestParam(value = "type", required = false) AuditType type,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Page<com.backend.helmaaibackend.domain.AuditLog> p = audit.list(type, q, page, size);
        Page<AuditLogView> mapped = p.map(l -> AuditLogView.builder()
                .id(l.getId())
                .type(l.getType())
                .userId(l.getUserId())
                .email(l.getEmail())
                .actorUserId(l.getActorUserId())
                .actorEmail(l.getActorEmail())
                .details(l.getDetails())
                .createdAt(l.getCreatedAt())
                .build());
        return ResponseEntity.ok(mapped);
    }

    @Operation(summary = "Create announcement (to all users or specific roles)")
    @PostMapping("/announce")
    public ResponseEntity<Void> announce(@Valid @RequestBody AnnouncementRequest req) {
        var me = actor();
        announcementService.create(req.getTitle(), req.getMessage(), req.getTargetRoles(), me.getId(), me.getEmail());
        return ResponseEntity.noContent().build();
    }
}
