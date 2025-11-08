package com.backend.helmaaibackend.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    private AuditType type;

    /** target user (subject of the action) */
    @Indexed
    private String userId;
    private String email;

    /** actor performing the action (admin/self) */
    @Indexed
    private String actorUserId;
    private String actorEmail;

    private String details;

    @CreatedDate
    private Instant createdAt;
}
