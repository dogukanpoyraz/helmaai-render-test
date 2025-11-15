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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());

        // CORS settings will be taken from the bean below
        http.cors(Customizer.withDefaults());

        http.addFilterBefore(
                new JwtAuthFilter(jwtService, userAccountRepository),
                BasicAuthenticationFilter.class
        );

        http.authorizeHttpRequests(auth -> auth
                // Swagger & OpenAPI
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()

                // Health
                .requestMatchers("/actuator/health").permitAll()

                // Public auth endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // Profile endpoints
                .requestMatchers("/api/profile", "/api/profile/**").authenticated()

                // Emergency endpoints
                .requestMatchers("/api/emergency/**").authenticated()

                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Preflight
                .requestMatchers("/options/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Everything else
                .anyRequest().authenticated()
        );

        http.sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.httpBasic(hb -> hb.disable());
        http.formLogin(form -> form.disable());
        http.logout(logout -> logout.disable());

        return http.build();
    }

    /**
     * CORS configuration:
     * Only the specified origins, headers and HTTP methods are allowed.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ALLOWED ORIGINS (protocol + domain + port)
        config.setAllowedOrigins(List.of(
                "https://helma-ai-website.web.app",
                "https://helma-ai-website.firebaseapp.com",
                "https://helma-ai.com",
                "http://localhost:3000"
        ));

        // ALLOWED HTTP METHODS
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // ALLOWED HEADERS
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));

        // So the frontend can read the Authorization header
        config.setExposedHeaders(List.of("Authorization"));

        // If you use cookies in the future, setting this to true will help; if using JWT via header, it's fine
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply to all endpoints
        source.registerCorsConfiguration("/**", config);
        return source;
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
