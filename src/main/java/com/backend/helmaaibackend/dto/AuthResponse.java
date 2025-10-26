package com.backend.helmaaibackend.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AuthResponse {
    private String token;   // JWT
    private String tokenType; // "Bearer"
    private UserView user;
}
