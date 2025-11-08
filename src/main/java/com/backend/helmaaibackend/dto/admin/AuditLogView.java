package com.backend.helmaaibackend.dto.admin;

import com.backend.helmaaibackend.domain.AuditType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AuditLogView {
    private String id;
    private AuditType type;
    private String userId;
    private String email;
    private String actorUserId;
    private String actorEmail;
    private String details;
    private Instant createdAt;
}
