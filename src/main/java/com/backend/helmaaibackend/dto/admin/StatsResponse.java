package com.backend.helmaaibackend.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class StatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long deletedUsers;
    private Map<String, Long> usersByRole; // key=Role.name()
}
