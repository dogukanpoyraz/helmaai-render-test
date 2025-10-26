package com.backend.helmaaibackend.domain;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyContact {
    private String name;
    private String relation;
    private String phone;
    private Integer priority;
}
