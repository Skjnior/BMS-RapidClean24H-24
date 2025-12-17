package com.rapidclean.web;

import com.rapidclean.entity.User;
import com.rapidclean.repository.UserRepository;
import com.rapidclean.service.AuditRequestInfo;
import com.rapidclean.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class AuditInterceptor implements HandlerInterceptor {
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private UserRepository userRepository;
    
    // Chemins à exclure de l'audit (ressources statiques, etc.)
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/css/", "/js/", "/images/", "/favicon.ico", "/error",
        "/api/debug/", "/actuator/"
    );
    
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Enregistrer le temps de début
        START_TIME.set(System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        try {
            String path = request.getRequestURI();
            
            // Ignorer les ressources statiques et certains chemins
            if (shouldExclude(path)) {
                return;
            }
            
            // Calculer le temps de réponse
            Long startTime = START_TIME.get();
            Long responseTime = startTime != null ? System.currentTimeMillis() - startTime : null;
            
            // Déterminer l'action et le type
            String action = determineAction(request);
            String actionType = determineActionType(request);
            String resourceType = determineResourceType(path);
            Long resourceId = extractResourceId(path);
            
            // Capturer toutes les informations de la requête AVANT traitement asynchrone
            AuditRequestInfo requestInfo = captureRequestInfo(request);
            
            // Récupérer l'utilisateur actuel
            User user = getCurrentUser();
            
            // Enregistrer le log d'audit
            if (user != null) {
                auditService.logActivity(
                    user,
                    requestInfo,
                    action,
                    actionType,
                    resourceType,
                    resourceId,
                    response.getStatus(),
                    responseTime,
                    ex != null ? ex.getMessage() : null
                );
            } else {
                // Log même pour les utilisateurs anonymes
                auditService.logActivity(
                    requestInfo,
                    action,
                    actionType,
                    resourceType,
                    resourceId,
                    response.getStatus(),
                    responseTime,
                    ex != null ? ex.getMessage() : null
                );
            }
            
        } catch (Exception e) {
            // Ne pas faire échouer la requête si l'audit échoue
            System.err.println("Erreur dans AuditInterceptor: " + e.getMessage());
        } finally {
            START_TIME.remove();
        }
    }
    
    /**
     * Vérifie si le chemin doit être exclu de l'audit
     */
    private boolean shouldExclude(String path) {
        if (path == null) {
            return true;
        }
        
        for (String excluded : EXCLUDED_PATHS) {
            if (path.startsWith(excluded)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Détermine l'action effectuée
     */
    private String determineAction(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        
        // Actions spéciales
        if (path.contains("/login")) {
            return "LOGIN";
        }
        if (path.contains("/logout")) {
            return "LOGOUT";
        }
        if (path.contains("/register")) {
            return "REGISTER";
        }
        
        // Actions CRUD basées sur la méthode HTTP
        switch (method) {
            case "GET":
                if (path.contains("/dashboard")) {
                    return "VIEW_DASHBOARD";
                } else if (path.contains("/detail") || path.contains("/view")) {
                    return "VIEW_DETAIL";
                } else if (path.contains("/edit")) {
                    return "VIEW_EDIT_FORM";
                } else {
                    return "VIEW";
                }
            case "POST":
                if (path.contains("/create") || path.contains("/add")) {
                    return "CREATE";
                } else if (path.contains("/update") || path.contains("/edit")) {
                    return "UPDATE";
                } else if (path.contains("/delete") || path.contains("/remove")) {
                    return "DELETE";
                } else {
                    return "POST";
                }
            case "PUT":
                return "UPDATE";
            case "DELETE":
                return "DELETE";
            case "PATCH":
                return "PATCH";
            default:
                return method;
        }
    }
    
    /**
     * Détermine le type d'action
     */
    private String determineActionType(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        if (path.contains("/login") || path.contains("/logout") || path.contains("/register")) {
            return "AUTHENTICATION";
        }
        
        if (method.equals("GET") && !path.contains("/api/")) {
            return "VIEW";
        }
        
        if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE") || method.equals("PATCH")) {
            return "MODIFICATION";
        }
        
        if (path.contains("/api/")) {
            return "API_CALL";
        }
        
        return "GENERAL";
    }
    
    /**
     * Détermine le type de ressource
     */
    private String determineResourceType(String path) {
        if (path.contains("/admin/users") || path.contains("/admin/employees")) {
            return "User";
        } else if (path.contains("/admin/services")) {
            return "Service";
        } else if (path.contains("/admin/requests") || path.contains("/client/request")) {
            return "ServiceRequest";
        } else if (path.contains("/admin/messages")) {
            return "Message";
        } else if (path.contains("/admin/reviews")) {
            return "Review";
        } else if (path.contains("/admin/notifications")) {
            return "Notification";
        } else if (path.contains("/employee/time-tracking")) {
            return "TimeTracking";
        } else if (path.contains("/employee/absences")) {
            return "Absence";
        } else if (path.contains("/employee/observations")) {
            return "WorkplaceObservation";
        } else if (path.contains("/admin/audit")) {
            return "AuditLog";
        } else if (path.contains("/dashboard")) {
            return "Dashboard";
        }
        
        return "Unknown";
    }
    
    /**
     * Extrait l'ID de la ressource depuis l'URL
     */
    private Long extractResourceId(String path) {
        try {
            // Pattern: /admin/users/123 ou /admin/employees/5/detail
            String[] parts = path.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                try {
                    Long id = Long.parseLong(parts[i]);
                    return id;
                } catch (NumberFormatException e) {
                    // Continue
                }
            }
        } catch (Exception e) {
            // Ignorer
        }
        return null;
    }
    
    /**
     * Récupère l'utilisateur actuellement authentifié
     */
    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                return userRepository.findByEmail(auth.getName()).orElse(null);
            }
        } catch (Exception e) {
            // Ignorer
        }
        return null;
    }
    
    /**
     * Capture toutes les informations de la requête avant traitement asynchrone
     */
    private AuditRequestInfo captureRequestInfo(HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String sessionId = null;
        if (request.getSession(false) != null) {
            sessionId = request.getSession(false).getId();
        }
        
        return new AuditRequestInfo(
            request.getMethod(),
            request.getRequestURI(),
            ipAddress,
            request.getHeader("User-Agent"),
            sessionId,
            request.getHeader("Referer"),
            request.getHeader("Accept-Language"),
            request.getQueryString()
        );
    }
    
    /**
     * Récupère l'adresse IP réelle du client
     */
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
        
        // Prendre la première IP si plusieurs sont présentes
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}

