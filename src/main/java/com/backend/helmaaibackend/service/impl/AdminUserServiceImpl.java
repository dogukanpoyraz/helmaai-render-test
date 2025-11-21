package com.backend.helmaaibackend.service.impl;

import com.backend.helmaaibackend.domain.Role;
import com.backend.helmaaibackend.domain.UserAccount;
import com.backend.helmaaibackend.dto.admin.AdminUserView;
import com.backend.helmaaibackend.dto.admin.StatsResponse;
import com.backend.helmaaibackend.repository.UserAccountRepository;
import com.backend.helmaaibackend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserAccountRepository userRepo;

    @Override
    public Page<AdminUserView> listUsers(String search, int page, int size, List<String> sort) {
        Sort sortObj = sort == null || sort.isEmpty()
                ? Sort.by(Sort.Direction.DESC, "createdAt")
                : Sort.by(sort.stream()
                .map(s -> s.startsWith("-") ? Sort.Order.desc(s.substring(1)) : Sort.Order.asc(s))
                .toList());

        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<UserAccount> p = (search == null || search.isBlank())
                ? userRepo.findByDeletedAtIsNull(pageable)
                : userRepo.searchActive(search.trim(), pageable);

        return p.map(this::toAdminView);
    }

    @Override
    public AdminUserView getUser(String userId) {
        UserAccount u = userRepo.findById(userId)
                .filter(x -> x.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return toAdminView(u);
    }

    @Override
    public AdminUserView updateStatus(String userId, boolean active) {
        UserAccount u = userRepo.findById(userId)
                .filter(x -> x.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        u.setActive(active);
        u = userRepo.save(u);
        return toAdminView(u);
    }

    @Override
    public AdminUserView updateRoles(String userId, List<Role> roles) {
        UserAccount u = userRepo.findById(userId)
                .filter(x -> x.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        u.setRoles(roles);
        u = userRepo.save(u);
        return toAdminView(u);
    }

    @Override
    public void softDelete(String userId) {
        UserAccount u = userRepo.findById(userId)
                .filter(x -> x.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        u.setDeletedAt(java.time.Instant.now());
        u.setActive(false);
        userRepo.save(u);
    }

    @Override
    public AdminUserView restore(String userId) {
        UserAccount u = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (u.getDeletedAt() == null) {
            throw new IllegalArgumentException("User is not deleted");
        }
        u.setDeletedAt(null);
        u.setActive(true);
        u = userRepo.save(u);
        return toAdminView(u);
    }

    @Override
    public void hardDelete(String userId) {
        if (!userRepo.existsById(userId)) {
            throw new NoSuchElementException("User not found");
        }
        userRepo.deleteById(userId);
    }

    @Override
    public AdminUserView deactivate(String userId) {
        UserAccount u = userRepo.findById(userId)
                .filter(x -> x.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        u.setActive(false);
        u = userRepo.save(u);
        return toAdminView(u);
    }

    @Override
    public StatsResponse stats() {
        long total = userRepo.count();
        long active = userRepo.countByActiveTrueAndDeletedAtIsNull();
        long deleted = userRepo.countByDeletedAtIsNotNull();

        EnumMap<Role, Long> byRole = new EnumMap<>(Role.class);
        for (Role r : Role.values()) {
            long c = userRepo.countByRolesContainingAndDeletedAtIsNull(r);
            byRole.put(r, c);
        }

        return StatsResponse.builder()
                .totalUsers(total)
                .activeUsers(active)
                .deletedUsers(deleted)
                .usersByRole(byRole.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                e -> e.getKey().name(),
                                java.util.Map.Entry::getValue
                        )))
                .build();
    }

    private AdminUserView toAdminView(UserAccount u) {
        return AdminUserView.builder()
                .id(u.getId())
                .roles(u.getRoles())
                .active(u.isActive())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .lastLoginAt(u.getLastLoginAt())
                .locale(u.getLocale())
                .timeZone(u.getTimeZone())
                .sttLang(u.getSttLang())
                .ttsVoice(u.getTtsVoice())
                .emergencyContacts(u.getEmergencyContacts())
                .profilePhotoUrl(u.getProfilePhotoUrl())
                .build();
    }
}
