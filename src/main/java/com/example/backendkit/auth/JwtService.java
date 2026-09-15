package com.example.backendkit.auth;

import com.example.backendkit.user.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final Key key; private final long accessSeconds;
    public JwtService(@Value("${app.security.jwt-secret}") String secret, @Value("${app.security.access-token-seconds:900}") long accessSeconds) {
        if (secret.length() < 32) throw new IllegalArgumentException("app.security.jwt-secret must be at least 32 characters");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.accessSeconds=accessSeconds;
    }
    public String createAccessToken(AppUser user) {
        Instant now=Instant.now();
        return Jwts.builder().subject(user.getId().toString()).claim("email", user.getEmail()).claim("roles", user.getRoles().stream().map(Enum::name).toList())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(accessSeconds))).signWith(key).compact();
    }
    public UUID subject(String token) { return UUID.fromString(parse(token).getSubject()); }
    public boolean isValid(String token) { try { parse(token); return true; } catch (RuntimeException ex) { return false; } }
    public long getAccessSeconds() { return accessSeconds; }
    private Claims parse(String token) { return Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token).getPayload(); }
}
