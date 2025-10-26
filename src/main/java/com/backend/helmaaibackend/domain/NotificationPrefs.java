package com.backend.helmaaibackend.domain;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationPrefs {
    private boolean push;
    private boolean email;
    private boolean sms;
}
