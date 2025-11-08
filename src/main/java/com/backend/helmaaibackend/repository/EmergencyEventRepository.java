package com.backend.helmaaibackend.repository;

import com.backend.helmaaibackend.domain.EmergencyEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Optional;

public interface EmergencyEventRepository extends MongoRepository<EmergencyEvent, String> {

    Optional<EmergencyEvent> findTopByUserIdOrderByCreatedAtDesc(String userId);

    long countByUserIdAndCreatedAtAfter(String userId, Instant after);
}
