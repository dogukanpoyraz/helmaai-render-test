package com.backend.helmaaibackend.repository;

import com.backend.helmaaibackend.domain.AuditLog;
import com.backend.helmaaibackend.domain.AuditType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditLog> findByTypeOrderByCreatedAtDesc(AuditType type, Pageable pageable);

    @Query("{ $or: [ " +
            " { 'details': { $regex: ?0, $options: 'i' } }, " +
            " { 'email':   { $regex: ?0, $options: 'i' } }, " +
            " { 'actorEmail': { $regex: ?0, $options: 'i' } } ] }")
    Page<AuditLog> search(String q, Pageable pageable);
}
