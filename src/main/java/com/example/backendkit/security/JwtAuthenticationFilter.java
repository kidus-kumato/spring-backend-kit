package com.example.backendkit.security;

import com.example.backendkit.auth.JwtService;
import com.example.backendkit.user.AppUser;
import com.example.backendkit.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt; private final UserRepository users;
    public JwtAuthenticationFilter(JwtService jwt, UserRepository users) { this.jwt=jwt; this.users=users; }
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header=request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token=header.substring(7);
            try { if (jwt.isValid(token)) { AppUser user=users.findById(jwt.subject(token)).orElse(null); if (user != null && user.getStatus() == com.example.backendkit.user.UserStatus.ACTIVE) {
                var authorities=user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_"+role.name())).toList();
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, authorities));
            } } } catch (RuntimeException ignored) { }
        }
        chain.doFilter(request,response);
    }
}
