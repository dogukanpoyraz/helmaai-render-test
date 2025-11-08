package com.backend.helmaaibackend.service.impl;

import com.backend.helmaaibackend.domain.AuditLog;
import com.backend.helmaaibackend.domain.AuditType;
import com.backend.helmaaibackend.repository.AuditLogRepository;
import com.backend.helmaaibackend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditRepo;

    @Override
    public void log(AuditType type, String userId, String email, String actorUserId, String actorEmail, String details) {
        AuditLog log = AuditLog.builder()
                .type(type)
                .userId(userId)
                .email(email)
                .actorUserId(actorUserId)
                .actorEmail(actorEmail)
                .details(details)
                .build();
        auditRepo.save(log);
    }

    @Override
    public Page<AuditLog> list(AuditType type, String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (type != null && (q == null || q.isBlank())) {
            return auditRepo.findByTypeOrderByCreatedAtDesc(type, pageable);
        } else if (type == null && q != null && !q.isBlank()) {
            return auditRepo.search(q.trim(), pageable);
        } else if (type != null && q != null && !q.isBlank()) {
            // if both filters are requested: first search by q, then filter by type in memory (simple & sufficient)
            return (Page<AuditLog>) auditRepo.search(q.trim(), pageable)
                    .map(x -> x)
                    .map(x -> x) // no-op to keep structure
                    .filter(x -> x.getType() == type);
        } else {
            return auditRepo.findAllByOrderByCreatedAtDesc(pageable);
        }
    }
}
