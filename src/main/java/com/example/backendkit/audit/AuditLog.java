package com.example.backendkit.audit;

import com.example.backendkit.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") private AppUser user;
    @Enumerated(EnumType.STRING) @Column(name="event_type", nullable=false, length=64) private AuditEventType eventType;
    @Column(name="ip_address", length=64) private String ipAddress;
    @Column(name="user_agent", length=512) private String userAgent;
    @Column(columnDefinition="text") private String metadata;
    @Column(name="created_at", nullable=false) private Instant createdAt=Instant.now();
    protected AuditLog() {}
    public AuditLog(AppUser user, AuditEventType type, String ip, String agent, String metadata) { this.user=user; eventType=type; ipAddress=ip; userAgent=agent; this.metadata=metadata; }
}
