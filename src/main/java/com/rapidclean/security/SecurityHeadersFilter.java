package com.rapidclean.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre pour ajouter des headers de sécurité HTTP
 */
@Component
@Order(2)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Content Security Policy
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net; " +
            "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://fonts.googleapis.com; " +
            "font-src 'self' https://fonts.gstatic.com https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
            "img-src 'self' data: https:; " +
            "connect-src 'self' https://cdn.jsdelivr.net; " +
            "frame-ancestors 'none';");
        
        // X-Content-Type-Options
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // X-Frame-Options
        response.setHeader("X-Frame-Options", "DENY");
        
        // X-XSS-Protection
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Referrer-Policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions-Policy
        response.setHeader("Permissions-Policy", 
            "geolocation=(), microphone=(), camera=()");
        
        filterChain.doFilter(request, response);
    }
}

