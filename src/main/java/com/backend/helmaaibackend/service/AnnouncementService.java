package com.backend.helmaaibackend.service;

import com.backend.helmaaibackend.domain.Announcement;

public interface AnnouncementService {
    Announcement create(String title, String message, java.util.List<com.backend.helmaaibackend.domain.Role> targetRoles,
                        String actorUserId, String actorEmail);
}
