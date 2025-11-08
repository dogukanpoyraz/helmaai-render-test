package com.backend.helmaaibackend.service;

import com.backend.helmaaibackend.domain.AuditLog;
import com.backend.helmaaibackend.domain.AuditType;
import org.springframework.data.domain.Page;

public interface AuditLogService {
    void log(AuditType type, String userId, String email, String actorUserId, String actorEmail, String details);
    Page<AuditLog> list(AuditType type, String q, int page, int size);
}
