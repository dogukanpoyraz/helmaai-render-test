package com.backend.helmaaibackend.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "emergency_events")
public class EmergencyEvent {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String userEmail;
    private String userFullName;

    private EmergencyStatus status;

    private Double lat;
    private Double lon;
    private Double accuracyMeters;

    private String note;
    private Instant triggeredAt;

    private List<EmergencyContact> contactsSnapshot;

    @CreatedDate
    private Instant createdAt;

    private Instant resolvedAt;     // optional
}
