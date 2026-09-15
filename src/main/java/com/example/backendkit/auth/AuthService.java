package com.example.backendkit.auth;

import com.example.backendkit.audit.AuditEventType;
import com.example.backendkit.audit.AuditService;
import com.example.backendkit.common.BusinessException;
import com.example.backendkit.user.AppUser;
import com.example.backendkit.user.UserRepository;
import com.example.backendkit.user.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users; private final RefreshTokenRepository refreshTokens; private final VerificationTokenRepository verificationTokens;
    private final PasswordResetTokenRepository resetTokens; private final PasswordEncoder encoder; private final JwtService jwt; private final TokenHasher hasher;
    private final EmailService email; private final AuditService audit; private final RateLimitService rateLimit; private final SecureRandom random=new SecureRandom();
    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens, VerificationTokenRepository verificationTokens,
            PasswordResetTokenRepository resetTokens, PasswordEncoder encoder, JwtService jwt, TokenHasher hasher, EmailService email,
            AuditService audit, RateLimitService rateLimit) { this.users=users; this.refreshTokens=refreshTokens; this.verificationTokens=verificationTokens; this.resetTokens=resetTokens; this.encoder=encoder; this.jwt=jwt; this.hasher=hasher; this.email=email; this.audit=audit; this.rateLimit=rateLimit; }

    @Transactional public void register(AuthDtos.RegisterRequest request, HttpServletRequest http) {
        checkLimit("register:"+ip(http)); String normalized=request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(normalized)) throw new BusinessException("EMAIL_ALREADY_REGISTERED", "An account with this email already exists", HttpStatus.CONFLICT);
        AppUser user=users.save(new AppUser(normalized, encoder.encode(request.password())));
        String token=randomToken(); verificationTokens.save(new VerificationToken(user, hasher.hash(token), Instant.now().plus(Duration.ofHours(24))));
        email.sendVerification(normalized, token); audit.record(user, AuditEventType.USER_REGISTERED, ip(http), agent(http), null);
    }

    @Transactional public AuthDtos.TokenResponse login(AuthDtos.LoginRequest request, HttpServletRequest http) {
        checkLimit("login:"+ip(http)); AppUser user=users.findByEmailIgnoreCase(request.email().trim()).orElse(null);
        if (user == null) { audit.record(null, AuditEventType.USER_LOGIN_FAILED, ip(http), agent(http), "unknown_email"); throw badCredentials(); }
        user.unlockIfExpired();
        if (user.getStatus() == UserStatus.LOCKED || user.getStatus() == UserStatus.INACTIVE || user.isTemporarilyLocked()) throw badCredentials();
        if (!encoder.matches(request.password(), user.getPasswordHash())) { user.recordFailedLogin(); if (user.getFailedLoginAttempts() >= 5) { user.lockUntil(Instant.now().plus(Duration.ofMinutes(15))); audit.record(user, AuditEventType.USER_LOCKED, ip(http), agent(http), "temporary_lock"); } users.save(user); audit.record(user, AuditEventType.USER_LOGIN_FAILED, ip(http), agent(http), null); throw badCredentials(); }
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) throw new BusinessException("EMAIL_NOT_VERIFIED", "Email verification is required", HttpStatus.FORBIDDEN);
        user.resetFailedLogins(); users.save(user); audit.record(user, AuditEventType.USER_LOGIN, ip(http), agent(http), null); return issue(user,http);
    }

    @Transactional public AuthDtos.TokenResponse refresh(AuthDtos.RefreshRequest request, HttpServletRequest http) {
        RefreshToken old=refreshTokens.findByTokenHash(hasher.hash(request.refreshToken())).filter(RefreshToken::isUsable).orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired", HttpStatus.UNAUTHORIZED));
        old.revoke(); refreshTokens.save(old); return issue(old.getUser(),http);
    }
    @Transactional public void logout(AuthDtos.RefreshRequest request) { refreshTokens.findByTokenHash(hasher.hash(request.refreshToken())).ifPresent(token -> { token.revoke(); refreshTokens.save(token); }); }
    @Transactional public AuthDtos.MessageResponse forgot(AuthDtos.ForgotPasswordRequest request, HttpServletRequest http) {
        checkLimit("forgot:"+ip(http)); users.findByEmailIgnoreCase(request.email().trim()).ifPresent(user -> { String token=randomToken(); resetTokens.save(new PasswordResetToken(user,hasher.hash(token),Instant.now().plus(Duration.ofMinutes(30)))); email.sendPasswordReset(user.getEmail(),token); });
        return new AuthDtos.MessageResponse("If an account exists for this email, a password reset link has been sent.");
    }
    @Transactional public void reset(AuthDtos.ResetPasswordRequest request, HttpServletRequest http) {
        checkLimit("reset:"+ip(http)); PasswordResetToken token=resetTokens.findByTokenHash(hasher.hash(request.token())).filter(PasswordResetToken::isUsable).orElseThrow(() -> new BusinessException("INVALID_RESET_TOKEN", "Reset token is invalid or expired", HttpStatus.BAD_REQUEST));
        AppUser user=token.getUser(); user.setPasswordHash(encoder.encode(request.newPassword())); users.save(user); token.markUsed(); resetTokens.save(token); refreshTokens.deleteAllByUserId(user.getId()); audit.record(user,AuditEventType.PASSWORD_RESET,ip(http),agent(http),null);
    }
    @Transactional public void verify(AuthDtos.VerifyEmailRequest request, HttpServletRequest http) {
        VerificationToken token=verificationTokens.findByTokenHash(hasher.hash(request.token())).filter(VerificationToken::isUsable).orElseThrow(() -> new BusinessException("INVALID_VERIFICATION_TOKEN", "Verification token is invalid or expired", HttpStatus.BAD_REQUEST));
        AppUser user=token.getUser(); if (user.getStatus() != UserStatus.PENDING_VERIFICATION) throw new BusinessException("EMAIL_ALREADY_VERIFIED", "Email is already verified", HttpStatus.CONFLICT);
        user.setStatus(UserStatus.ACTIVE); users.save(user); token.markUsed(); verificationTokens.save(token); audit.record(user,AuditEventType.EMAIL_VERIFIED,ip(http),agent(http),null);
    }
    @Transactional public void resend(AuthDtos.ForgotPasswordRequest request, HttpServletRequest http) {
        users.findByEmailIgnoreCase(request.email().trim()).filter(u -> u.getStatus()==UserStatus.PENDING_VERIFICATION).ifPresent(user -> { String token=randomToken(); verificationTokens.save(new VerificationToken(user,hasher.hash(token),Instant.now().plus(Duration.ofHours(24)))); email.sendVerification(user.getEmail(),token); });
    }
    private AuthDtos.TokenResponse issue(AppUser user,HttpServletRequest http) { String access=jwt.createAccessToken(user), raw=randomToken(); refreshTokens.save(new RefreshToken(user,hasher.hash(raw),Instant.now().plus(Duration.ofDays(30)),ip(http),agent(http))); return new AuthDtos.TokenResponse(access,raw,"Bearer",jwt.getAccessSeconds()); }
    private void checkLimit(String key) { if (!rateLimit.allow(key,5,Duration.ofMinutes(1))) throw new BusinessException("RATE_LIMITED","Too many requests; please try again later",HttpStatus.TOO_MANY_REQUESTS); }
    private String randomToken() { byte[] bytes=new byte[48]; random.nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    private String ip(HttpServletRequest r) { return r.getRemoteAddr(); } private String agent(HttpServletRequest r) { return r.getHeader("User-Agent"); }
    private BusinessException badCredentials() { return new BusinessException("AUTHENTICATION_FAILED","Authentication failed",HttpStatus.UNAUTHORIZED); }
}
