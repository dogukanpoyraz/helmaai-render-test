package com.backend.helmaaibackend.service;

import com.backend.helmaaibackend.dto.admin.AdminUserView;
import com.backend.helmaaibackend.dto.admin.StatsResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminUserService {
    Page<AdminUserView> listUsers(String search, int page, int size, List<String> sort);
    AdminUserView getUser(String userId);
    AdminUserView updateStatus(String userId, boolean active);
    AdminUserView updateRoles(String userId, List<com.backend.helmaaibackend.domain.Role> roles);
    void softDelete(String userId);
    AdminUserView restore(String userId);

    void hardDelete(String userId);
    AdminUserView deactivate(String userId);
    StatsResponse stats();
}
