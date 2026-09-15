package com.example.backendkit.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth=auth; }
    @PostMapping("/register") public ResponseEntity<AuthDtos.MessageResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest r,HttpServletRequest h) { auth.register(r,h); return ResponseEntity.status(HttpStatus.CREATED).body(new AuthDtos.MessageResponse("Registration successful; verify your email to activate the account.")); }
    @PostMapping("/login") public AuthDtos.TokenResponse login(@Valid @RequestBody AuthDtos.LoginRequest r,HttpServletRequest h) { return auth.login(r,h); }
    @PostMapping("/refresh") public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest r,HttpServletRequest h) { return auth.refresh(r,h); }
    @PostMapping("/logout") public ResponseEntity<Void> logout(@Valid @RequestBody AuthDtos.RefreshRequest r) { auth.logout(r); return ResponseEntity.noContent().build(); }
    @PostMapping("/forgot-password") public AuthDtos.MessageResponse forgot(@Valid @RequestBody AuthDtos.ForgotPasswordRequest r,HttpServletRequest h) { return auth.forgot(r,h); }
    @PostMapping("/reset-password") public ResponseEntity<Void> reset(@Valid @RequestBody AuthDtos.ResetPasswordRequest r,HttpServletRequest h) { auth.reset(r,h); return ResponseEntity.noContent().build(); }
    @PostMapping("/verify-email") public ResponseEntity<Void> verify(@Valid @RequestBody AuthDtos.VerifyEmailRequest r,HttpServletRequest h) { auth.verify(r,h); return ResponseEntity.noContent().build(); }
    @PostMapping("/resend-verification") public AuthDtos.MessageResponse resend(@Valid @RequestBody AuthDtos.ForgotPasswordRequest r,HttpServletRequest h) { auth.resend(r,h); return new AuthDtos.MessageResponse("If an account requires verification, a new verification link has been sent."); }
}
