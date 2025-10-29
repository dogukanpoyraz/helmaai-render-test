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
 * This filter does the following:
 * - Gets Authorization: Bearer <jwt> header
 * - Validates JWT
 * - Extracts userId from subject
 * - Finds user from DB
 * - Sets authentication info in SecurityContext
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

        // 1. Extract token from header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            // No token doesn't force error; might be a public endpoint.
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Validate token / extract userId
            String userId = jwtService.extractUserId(token);

            // 3. Find user from DB
            UserAccount user = userRepo.findById(userId).orElse(null);
            if (user == null || !user.isActive()) {
                filterChain.doFilter(request, response);
                return;
            }

            // 4. Convert user roles -> Spring Authority list
            List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(roleEnum -> new SimpleGrantedAuthority("ROLE_" + roleEnum.name()))
                    .collect(Collectors.toList());

            // 5. Prepare Authentication object
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

            // 6. Set in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // If token fails: don't set context. Request behaves as public.
            SecurityContextHolder.clearContext();
        }

        // 7. Continue chain
        filterChain.doFilter(request, response);
    }
}
