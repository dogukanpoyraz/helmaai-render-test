package com.backend.helmaaibackend.controller;

import com.backend.helmaaibackend.domain.UserAccount;
import com.backend.helmaaibackend.dto.UserView;
import com.backend.helmaaibackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile", description = "Aktif kullanıcının bilgileri")
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Aktif kullanıcının profilini getir",
            description = "Header'daki Bearer JWT token'dan kullanıcıyı çözer ve UserView döner."
    )
    @GetMapping
    public ResponseEntity<UserView> me() {
        // SecurityContext’ten UserAccount çek
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserAccount user)) {
            // bu zaten normalde authenticated path'te olmamalı ama yine de guard koyalım
            return ResponseEntity.status(401).build();
        }

        // userService.toView(String id) zaten güvenli UserView döndürüyor
        UserView view = userService.toView(user.getId());
        return ResponseEntity.ok(view);
    }
}
