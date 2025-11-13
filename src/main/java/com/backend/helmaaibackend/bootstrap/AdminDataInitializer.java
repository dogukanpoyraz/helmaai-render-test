// src/main/java/com/backend/helmaaibackend/bootstrap/AdminDataInitializer.java
package com.backend.helmaaibackend.bootstrap;

import com.backend.helmaaibackend.domain.Role;
import com.backend.helmaaibackend.domain.UserAccount;
import com.backend.helmaaibackend.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.admin.seed-enabled", havingValue = "true", matchIfMissing = true)
public class AdminDataInitializer implements ApplicationRunner {

    private final UserAccountRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.full-name:SystemAdministrator}")
    private String adminFullName;

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (adminEmail == null || adminEmail.isBlank() ||
                    adminPassword == null || adminPassword.isBlank()) {
                return;
            }
            if (userRepo.existsByEmail(adminEmail)) {
                return;
            }
            UserAccount admin = UserAccount.builder()
                    .email(adminEmail)
                    .fullName(adminFullName)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .roles(List.of(Role.ADMIN))
                    .active(true)
                    .build();
            userRepo.save(admin);
            System.out.println("[AdminDataInitializer] ADMIN created: " + adminEmail);
        } catch (Exception e) {
            System.err.println("[AdminDataInitializer] Seed failed: " + e.getMessage());
        }
    }
}
