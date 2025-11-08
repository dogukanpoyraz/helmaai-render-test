package com.backend.helmaaibackend.repository;

import com.backend.helmaaibackend.domain.Announcement;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnnouncementRepository extends MongoRepository<Announcement, String> {
}
