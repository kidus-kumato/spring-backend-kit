package com.example.backendkit.auth;

import com.example.backendkit.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "email_verification_tokens")
public class VerificationToken {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private AppUser user;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "used_at") private Instant usedAt;
    protected VerificationToken() {}
    public VerificationToken(AppUser user, String hash, Instant expiresAt) { this.user=user; tokenHash=hash; this.expiresAt=expiresAt; }
    public AppUser getUser() { return user; }
    public boolean isUsable() { return usedAt == null && expiresAt.isAfter(Instant.now()); }
    public void markUsed() { usedAt=Instant.now(); }
}
