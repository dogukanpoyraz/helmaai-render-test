package com.backend.helmaaibackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
@CompoundIndexes({
        @CompoundIndex(name = "u_unique_email", def = "{'email': 1}", unique = true, sparse = true)
})
public class UserAccount {

    @Id
    private String id;

    @JsonIgnore
    private String passwordHash;

    private List<Role> roles;

    private boolean active;

    private String fullName;
    private String email;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant lastLoginAt;

    private String locale;
    private String timeZone;
    private String sttLang;
    private String ttsVoice;

    private List<EmergencyContact> emergencyContacts;

    @JsonIgnore
    private Instant deletedAt;
}
