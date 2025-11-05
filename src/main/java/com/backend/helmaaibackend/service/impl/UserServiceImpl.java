package com.backend.helmaaibackend.service.impl;

import com.backend.helmaaibackend.domain.Role;
import com.backend.helmaaibackend.domain.UserAccount;
import com.backend.helmaaibackend.dto.*;
import com.backend.helmaaibackend.exception.BadRequestException;
import com.backend.helmaaibackend.repository.UserAccountRepository;
import com.backend.helmaaibackend.security.JwtService;
import com.backend.helmaaibackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        List<Role> roles = Optional.ofNullable(req.getRoles())
                .filter(list -> !list.isEmpty())
                .orElse(List.of(Role.ELDER));

        boolean active = req.getActive() == null ? true : req.getActive();

        UserAccount entity = UserAccount.builder()
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .roles(roles)
                .active(active)
                .fullName(req.getFullName())
                .email(req.getEmail())
                // createdAt / updatedAt => will be filled by auditing
                .locale(defaultIfBlank(req.getLocale(), "tr-TR"))
                .timeZone(defaultIfBlank(req.getTimeZone(), "Europe/Istanbul"))
                .sttLang(defaultIfBlank(req.getSttLang(), "tr-TR"))
                .ttsVoice(defaultIfBlank(req.getTtsVoice(), "tr-TR-Standard-A"))
                .emergencyContacts(req.getEmergencyContacts())
                .build();

        entity = userRepo.save(entity);

        UserView view = toView(entity);

        String token = jwtService.generate(
                entity.getId(),
                Map.of(
                        "roles", entity.getRoles(),
                        "email", entity.getEmail()
                )
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(view)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        UserAccount user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.isActive()) {
            throw new IllegalStateException("User is not active");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        user.setLastLoginAt(Instant.now());
        userRepo.save(user); // updatedAt will be set automatically

        UserView view = toView(user);

        String token = jwtService.generate(
                user.getId(),
                Map.of(
                        "roles", user.getRoles(),
                        "email", user.getEmail()
                )
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(view)
                .build();
    }

    @Override
    public UserView toView(String userId) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return toView(user);
    }

    @Override
    public UserView updateProfile(String userId, UpdateProfileRequest request) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getLocale() != null && !request.getLocale().isBlank()) {
            user.setLocale(request.getLocale());
        }
        if (request.getTimeZone() != null && !request.getTimeZone().isBlank()) {
            user.setTimeZone(request.getTimeZone());
        }
        if (request.getSttLang() != null && !request.getSttLang().isBlank()) {
            user.setSttLang(request.getSttLang());
        }
        if (request.getTtsVoice() != null && !request.getTtsVoice().isBlank()) {
            user.setTtsVoice(request.getTtsVoice());
        }

        userRepo.save(user); // updatedAt will be updated by auditing

        return toView(user);
    }

    @Override
    public void updatePassword(String userId, UpdatePasswordRequest request) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Old password is not correct");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user); // updatedAt will be updated by auditing
    }

    @Override
    public UserView updateEmergencyContacts(String userId, UpdateEmergencyContactsRequest request) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        user.setEmergencyContacts(request.getEmergencyContacts());
        userRepo.save(user); // updatedAt will be updated by auditing

        return toView(user);
    }

    // deactivate
    @Override
    public void deactivateAccount(String userId) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        user.setActive(false);
        userRepo.save(user); // updatedAt will be updated by auditing
    }

    private UserView toView(UserAccount u) {
        return UserView.builder()
                .id(u.getId())
                .roles(u.getRoles())
                .active(u.isActive())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .lastLoginAt(u.getLastLoginAt())
                .locale(u.getLocale())
                .timeZone(u.getTimeZone())
                .sttLang(u.getSttLang())
                .ttsVoice(u.getTtsVoice())
                .emergencyContacts(u.getEmergencyContacts())
                .build();
    }

    private String defaultIfBlank(String val, String def) {
        return (val == null || val.isBlank()) ? def : val;
    }
}
