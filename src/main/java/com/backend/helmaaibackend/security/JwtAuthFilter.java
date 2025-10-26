package com.backend.helmaaibackend.security;

import com.backend.helmaaibackend.domain.UserAccount;
import com.backend.helmaaibackend.repository.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bu filter şunu yapar:
 * - Authorization: Bearer <jwt> header'ını alır
 * - JWT'yi doğrular
 * - subject içinden userId çıkarır
 * - DB'den user'ı bulur
 * - SecurityContext'e kimlik bilgisini koyar
 */
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserAccountRepository userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Header'dan token çek
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            // Token yoksa zorlamıyoruz; public endpoint olabilir.
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Token doğrula / içinden userId çek
            String userId = jwtService.extractUserId(token);

            // 3. DB'den kullanıcıyı bul
            UserAccount user = userRepo.findById(userId).orElse(null);
            if (user == null || !user.isActive()) {
                filterChain.doFilter(request, response);
                return;
            }

            // 4. Kullanıcının rolleri -> Spring Authority listesine çevir
            List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(roleEnum -> new SimpleGrantedAuthority("ROLE_" + roleEnum.name()))
                    .collect(Collectors.toList());

            // 5. Authentication objesi hazırla
            AbstractAuthenticationToken authentication =
                    new AbstractAuthenticationToken(authorities) {
                        @Override
                        public Object getCredentials() {
                            return token;
                        }

                        @Override
                        public Object getPrincipal() {
                            return user;
                        }
                    };

            authentication.setAuthenticated(true);

            // 6. SecurityContext'e koy
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // Token patlarsa: context set etmiyoruz. Request public gibi davranır.
            SecurityContextHolder.clearContext();
        }

        // 7. zincir devam
        filterChain.doFilter(request, response);
    }
}
