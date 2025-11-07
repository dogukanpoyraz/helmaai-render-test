package com.backend.helmaaibackend.dto.admin;

import com.backend.helmaaibackend.domain.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRolesRequest {
    @NotNull
    @Size(min = 1)
    private List<Role> roles;
}
