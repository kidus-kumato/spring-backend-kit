package com.example.backendkit.auth;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> { Optional<VerificationToken> findByTokenHash(String hash); }
