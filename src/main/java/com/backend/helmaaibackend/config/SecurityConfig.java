package com.backend.helmaaibackend.config;

import com.backend.helmaaibackend.repository.UserAccountRepository;
import com.backend.helmaaibackend.security.JwtAuthFilter;
import com.backend.helmaaibackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());

        // JWT filterimizi Security zincirine ekliyoruz
        http.addFilterBefore(
                new JwtAuthFilter(jwtService, userAccountRepository),
                BasicAuthenticationFilter.class
        );

        http.authorizeHttpRequests(auth -> auth
                // Swagger & OpenAPI (herkese açık)
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()

                // Health check (opsiyonel açık)
                .requestMatchers("/actuator/health").permitAll()

                // Auth uçları (register/login herkese açık)
                .requestMatchers("/api/auth/**").permitAll()

                // /api/me ve alt path'leri => sadece giriş yapmış kullanıcılar
                .requestMatchers("/api/me", "/api/me/**").authenticated()

                // CORS preflight
                .requestMatchers("/options/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // geri kalan her şey default olarak korumalı
                .anyRequest().authenticated()
        );

        // Stateless session (JWT kullanıyoruz)
        http.sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // form login / basic auth yok
        http.httpBasic(hb -> hb.disable());
        http.formLogin(form -> form.disable());
        http.logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
