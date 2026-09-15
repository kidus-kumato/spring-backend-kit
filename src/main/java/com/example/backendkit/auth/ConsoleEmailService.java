package com.example.backendkit.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service @Profile("!smtp")
public class ConsoleEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailService.class);
    public void sendVerification(String email, String token) { log.info("email_event=verification_recipient={} token={}", email, token); }
    public void sendPasswordReset(String email, String token) { log.info("email_event=password_reset_recipient={} token={}", email, token); }
}
