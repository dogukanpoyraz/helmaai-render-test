package com.backend.helmaaibackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long expMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.issuer}") String issuer,
                      @Value("${app.jwt.exp-minutes}") long expMinutes) {

        // secret -> base64 -> key
        this.key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(
                        java.util.Base64.getEncoder().encodeToString(secret.getBytes())
                )
        );
        this.issuer = issuer;
        this.expMinutes = expMinutes;
    }

    // Token generation
    public String generate(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expMinutes * 60);

        return Jwts.builder()
                .setSubject(subject)           // subject = userId
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Token parse & validate
    public Jws<Claims> parseAndValidate(String token) {
        return Jwts.parserBuilder()
                .requireIssuer(issuer)
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    // Extract userId (subject) from token
    public String extractUserId(String token) {
        return parseAndValidate(token).getBody().getSubject();
    }
}
