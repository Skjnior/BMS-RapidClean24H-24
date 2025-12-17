package com.rapidclean.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filtre de rate limiting pour protéger contre les attaques brute force
 * Limite le nombre de tentatives de connexion par IP
 */
@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    
    // Limite de tentatives par IP
    private static final int MAX_ATTEMPTS = 5;
    // Fenêtre de temps en millisecondes (15 minutes)
    private static final long TIME_WINDOW_MS = 15 * 60 * 1000;
    
    // Stockage des tentatives par IP (en mémoire)
    private final Map<String, AttemptInfo> attemptsByIp = new ConcurrentHashMap<>();
    
    // Chemins à protéger
    private static final String[] PROTECTED_PATHS = {
        "/admin-secret-access",
        "/employee-login",
        "/api/auth/employee-login",
        "/login"
    };

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        // Vérifier si c'est une tentative de connexion
        if (isLoginAttempt(requestPath, method)) {
            String clientIp = getClientIpAddress(request);
            
            if (isBlocked(clientIp)) {
                logger.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, requestPath);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Trop de tentatives. Veuillez réessayer dans quelques minutes.\"}");
                return;
            }
            
            // Enregistrer la tentative
            recordAttempt(clientIp, requestPath);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isLoginAttempt(String path, String method) {
        if (!"POST".equals(method)) {
            return false;
        }
        
        for (String protectedPath : PROTECTED_PATHS) {
            if (path.contains(protectedPath)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isBlocked(String ip) {
        AttemptInfo info = attemptsByIp.get(ip);
        if (info == null) {
            return false;
        }
        
        // Nettoyer les anciennes tentatives
        long now = System.currentTimeMillis();
        if (now - info.getFirstAttemptTime() > TIME_WINDOW_MS) {
            attemptsByIp.remove(ip);
            return false;
        }
        
        return info.getAttemptCount() >= MAX_ATTEMPTS;
    }
    
    private void recordAttempt(String ip, String path) {
        long now = System.currentTimeMillis();
        attemptsByIp.compute(ip, (key, existingInfo) -> {
            if (existingInfo == null) {
                return new AttemptInfo(now);
            }
            
            // Réinitialiser si la fenêtre de temps est expirée
            if (now - existingInfo.getFirstAttemptTime() > TIME_WINDOW_MS) {
                return new AttemptInfo(now);
            }
            
            existingInfo.incrementAttempts();
            return existingInfo;
        });
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * Classe interne pour stocker les informations de tentatives
     */
    private static class AttemptInfo {
        private final long firstAttemptTime;
        private final AtomicInteger attemptCount;
        
        public AttemptInfo(long firstAttemptTime) {
            this.firstAttemptTime = firstAttemptTime;
            this.attemptCount = new AtomicInteger(1);
        }
        
        public long getFirstAttemptTime() {
            return firstAttemptTime;
        }
        
        public int getAttemptCount() {
            return attemptCount.get();
        }
        
        public void incrementAttempts() {
            attemptCount.incrementAndGet();
        }
    }
}

