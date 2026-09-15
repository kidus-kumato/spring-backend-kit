package com.example.backendkit.auth;

import com.example.backendkit.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "refresh_tokens")
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private AppUser user;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "revoked_at") private Instant revokedAt;
    @Column(name = "ip_address", length = 64) private String ipAddress;
    @Column(name = "user_agent", length = 512) private String userAgent;
    protected RefreshToken() {}
    public RefreshToken(AppUser user, String hash, Instant expiresAt, String ip, String agent) { this.user=user; tokenHash=hash; this.expiresAt=expiresAt; createdAt=Instant.now(); ipAddress=ip; userAgent=agent; }
    public AppUser getUser() { return user; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public void revoke() { revokedAt = Instant.now(); }
    public boolean isUsable() { return revokedAt == null && expiresAt.isAfter(Instant.now()); }
}
