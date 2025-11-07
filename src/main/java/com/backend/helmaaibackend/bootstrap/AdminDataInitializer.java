package com.backend.helmaaibackend.bootstrap;

import com.backend.helmaaibackend.domain.Role;
import com.backend.helmaaibackend.domain.UserAccount;
import com.backend.helmaaibackend.repository.UserAccountRepository;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements ApplicationRunner {

    private final UserAccountRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // .env init
        Dotenv dotenv = Dotenv.load();

        String adminEmail = dotenv.get("APP_ADMIN_EMAIL");
        String adminPassword = dotenv.get("APP_ADMIN_PASSWORD");
        String adminFullName = dotenv.get("APP_ADMIN_FULL_NAME", "System Administrator");

        if (adminEmail == null || adminEmail.isBlank() ||
                adminPassword == null || adminPassword.isBlank()) {
            System.out.println("Admin seed skipped: APP_ADMIN_EMAIL or APP_ADMIN_PASSWORD missing in .env");
            return;
        }

        if (userRepo.existsByEmail(adminEmail)) {
            System.out.println("Admin account already exists: " + adminEmail);
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
        System.out.println("Admin account created: " + adminEmail);
    }
}
