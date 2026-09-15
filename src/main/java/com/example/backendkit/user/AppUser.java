package com.example.backendkit.user;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 320)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "first_name", length = 100) private String firstName;
    @Column(name = "last_name", length = 100) private String lastName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role"}))
    @Column(name = "role", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private Set<RoleName> roles = new HashSet<>();
    @Column(name = "failed_login_attempts", nullable = false) private int failedLoginAttempts;
    @Column(name = "locked_until") private Instant lockedUntil;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected AppUser() {}
    public AppUser(String email, String passwordHash) {
        this.email = email; this.passwordHash = passwordHash; this.roles.add(RoleName.USER);
        this.createdAt = Instant.now(); this.updatedAt = this.createdAt;
    }
    public void touch() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public UserStatus getStatus() { return status; }
    public Set<RoleName> getRoles() { return roles; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public Instant getLockedUntil() { return lockedUntil; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setPasswordHash(String value) { passwordHash = value; touch(); }
    public void setFirstName(String value) { firstName = value; touch(); }
    public void setLastName(String value) { lastName = value; touch(); }
    public void setStatus(UserStatus value) { status = value; touch(); }
    public void recordFailedLogin() { failedLoginAttempts++; touch(); }
    public void resetFailedLogins() { failedLoginAttempts = 0; lockedUntil = null; touch(); }
    public void lockUntil(Instant value) { status = UserStatus.LOCKED; lockedUntil = value; touch(); }
    public boolean isTemporarilyLocked() { return lockedUntil != null && lockedUntil.isAfter(Instant.now()); }
    public void unlockIfExpired() { if (status == UserStatus.LOCKED && !isTemporarilyLocked()) { status = UserStatus.ACTIVE; resetFailedLogins(); } }
}
