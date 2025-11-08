package com.backend.helmaaibackend.dto.admin;

import com.backend.helmaaibackend.domain.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AnnouncementRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    /** optional */
    private List<Role> targetRoles;
}
