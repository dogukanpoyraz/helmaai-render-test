package com.backend.helmaaibackend.service.impl;

import com.backend.helmaaibackend.domain.Announcement;
import com.backend.helmaaibackend.domain.Role;
import com.backend.helmaaibackend.domain.AuditType;
import com.backend.helmaaibackend.repository.AnnouncementRepository;
import com.backend.helmaaibackend.service.AnnouncementService;
import com.backend.helmaaibackend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository repo;
    private final AuditLogService audit;

    @Override
    public Announcement create(String title, String message, List<Role> targetRoles,
                               String actorUserId, String actorEmail) {
        Announcement a = Announcement.builder()
                .title(title)
                .message(message)
                .targetRoles(targetRoles == null || targetRoles.isEmpty() ? null : targetRoles)
                .createdByUserId(actorUserId)
                .createdByEmail(actorEmail)
                .build();
        a = repo.save(a);

        audit.log(AuditType.ANNOUNCEMENT_CREATE, null, null, actorUserId, actorEmail,
                "Announcement created: " + title);

        return a;
    }
}
