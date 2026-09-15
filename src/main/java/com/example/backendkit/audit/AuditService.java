package com.example.backendkit.audit;

import com.example.backendkit.user.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository repository;
    public AuditService(AuditLogRepository repository) { this.repository=repository; }
    @Transactional public void record(AppUser user, AuditEventType type, String ip, String agent, String metadata) {
        repository.save(new AuditLog(user, type, ip, agent, metadata));
    }
}
