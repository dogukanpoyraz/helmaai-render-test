package com.backend.helmaaibackend.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "announcements")
public class Announcement {

    @Id
    private String id;

    private String title;
    private String message;

    private List<Role> targetRoles;

    private String createdByUserId;
    private String createdByEmail;

    @CreatedDate
    private Instant createdAt;
}
