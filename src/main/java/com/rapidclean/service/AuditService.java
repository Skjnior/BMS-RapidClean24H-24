package com.rapidclean.service;

import com.rapidclean.entity.AuditLog;
import com.rapidclean.entity.User;
import com.rapidclean.repository.AuditLogRepository;
import com.rapidclean.repository.AuditLogSpecification;
import com.rapidclean.service.AuditRequestInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    /**
     * Enregistre un log d'audit de manière asynchrone
     */
    @Async
    @Transactional
    public void logActivity(HttpServletRequest request, String action, String actionType, 
                           String resourceType, Long resourceId, Integer statusCode, 
                           Long responseTimeMs, String errorMessage) {
        try {
            AuditLog log = new AuditLog();
            
            // Utilisateur actuel
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                // On récupère l'utilisateur depuis la session si disponible
                // Sinon on stocke juste l'email
            }
            
            // Informations de base
            log.setAction(action);
            log.setActionType(actionType);
            log.setResourceType(resourceType);
            log.setResourceId(resourceId);
            log.setRequestMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURI());
            log.setStatusCode(statusCode);
            log.setResponseTimeMs(responseTimeMs);
            log.setErrorMessage(errorMessage);
            
            // IP Address
            String ipAddress = getClientIpAddress(request);
            log.setIpAddress(ipAddress);
            
            // User Agent et parsing
            String userAgent = request.getHeader("User-Agent");
            log.setUserAgent(userAgent);
            parseUserAgent(log, userAgent);
            
            // Informations de session
            if (request.getSession(false) != null) {
                log.setSessionId(request.getSession(false).getId());
            }
            
            // Headers
            log.setReferer(request.getHeader("Referer"));
            log.setAcceptLanguage(request.getHeader("Accept-Language"));
            
            // Paramètres de requête
            if (request.getQueryString() != null) {
                log.setRequestParams(request.getQueryString());
            }
            
            // Détection du pays (basique - peut être amélioré avec une API GeoIP)
            detectLocation(log, ipAddress);
            
            // Timestamp
            log.setTimestamp(LocalDateTime.now());
            
            // Sauvegarder
            auditLogRepository.save(log);
            
        } catch (Exception e) {
            // Ne pas faire échouer la requête si l'audit échoue
            System.err.println("Erreur lors de l'enregistrement de l'audit: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Enregistre un log d'audit simple
     */
    @Async
    @Transactional
    public void logActivity(HttpServletRequest request, String action) {
        logActivity(request, action, null, null, null, null, null, null);
    }
    
    /**
     * Enregistre un log d'audit avec utilisateur
     */
    @Async
    @Transactional
    public void logActivity(User user, HttpServletRequest request, String action, 
                           String actionType, String resourceType, Long resourceId) {
        try {
            AuditLog log = new AuditLog();
            log.setUser(user);
            log.setAction(action);
            log.setActionType(actionType);
            log.setResourceType(resourceType);
            log.setResourceId(resourceId);
            log.setRequestMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURI());
            
            // IP Address
            String ipAddress = getClientIpAddress(request);
            log.setIpAddress(ipAddress);
            
            // User Agent
            String userAgent = request.getHeader("User-Agent");
            log.setUserAgent(userAgent);
            parseUserAgent(log, userAgent);
            
            // Session
            if (request.getSession(false) != null) {
                log.setSessionId(request.getSession(false).getId());
            }
            
            // Headers
            log.setReferer(request.getHeader("Referer"));
            log.setAcceptLanguage(request.getHeader("Accept-Language"));
            
            // Paramètres
            if (request.getQueryString() != null) {
                log.setRequestParams(request.getQueryString());
            }
            
            // Location
            detectLocation(log, ipAddress);
            
            // Timestamp
            log.setTimestamp(LocalDateTime.now());
            
            auditLogRepository.save(log);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement de l'audit: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Enregistre un log d'audit avec utilisateur et réponse HTTP
     */
    @Async
    @Transactional
    public void logActivity(User user, HttpServletRequest request, String action, 
                           String actionType, String resourceType, Long resourceId,
                           Integer statusCode, Long responseTimeMs, String errorMessage) {
        try {
            AuditLog log = new AuditLog();
            log.setUser(user);
            log.setAction(action);
            log.setActionType(actionType);
            log.setResourceType(resourceType);
            log.setResourceId(resourceId);
            log.setRequestMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURI());
            log.setStatusCode(statusCode);
            log.setResponseTimeMs(responseTimeMs);
            log.setErrorMessage(errorMessage);
            
            // IP Address
            String ipAddress = getClientIpAddress(request);
            log.setIpAddress(ipAddress);
            
            // User Agent
            String userAgent = request.getHeader("User-Agent");
            log.setUserAgent(userAgent);
            parseUserAgent(log, userAgent);
            
            // Session
            if (request.getSession(false) != null) {
                log.setSessionId(request.getSession(false).getId());
            }
            
            // Headers
            log.setReferer(request.getHeader("Referer"));
            log.setAcceptLanguage(request.getHeader("Accept-Language"));
            
            // Paramètres
            if (request.getQueryString() != null) {
                log.setRequestParams(request.getQueryString());
            }
            
            // Location
            detectLocation(log, ipAddress);
            
            // Timestamp
            log.setTimestamp(LocalDateTime.now());
            
            auditLogRepository.save(log);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement de l'audit: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Enregistre un log d'audit avec utilisateur et réponse HTTP (utilise AuditRequestInfo)
     */
    @Async
    @Transactional
    public void logActivity(User user, AuditRequestInfo requestInfo, String action, 
                           String actionType, String resourceType, Long resourceId,
                           Integer statusCode, Long responseTimeMs, String errorMessage) {
        try {
            AuditLog log = new AuditLog();
            log.setUser(user);
            log.setAction(action);
            log.setActionType(actionType);
            log.setResourceType(resourceType);
            log.setResourceId(resourceId);
            log.setRequestMethod(requestInfo.getMethod());
            log.setRequestUrl(requestInfo.getRequestUrl());
            log.setStatusCode(statusCode);
            log.setResponseTimeMs(responseTimeMs);
            log.setErrorMessage(errorMessage);
            
            // IP Address
            log.setIpAddress(requestInfo.getIpAddress());
            
            // User Agent
            log.setUserAgent(requestInfo.getUserAgent());
            parseUserAgent(log, requestInfo.getUserAgent());
            
            // Session
            log.setSessionId(requestInfo.getSessionId());
            
            // Headers
            log.setReferer(requestInfo.getReferer());
            log.setAcceptLanguage(requestInfo.getAcceptLanguage());
            
            // Paramètres
            log.setRequestParams(requestInfo.getQueryString());
            
            // Location
            detectLocation(log, requestInfo.getIpAddress());
            
            // Timestamp
            log.setTimestamp(LocalDateTime.now());
            
            auditLogRepository.save(log);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement de l'audit: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Enregistre un log d'audit sans utilisateur (utilise AuditRequestInfo)
     */
    @Async
    @Transactional
    public void logActivity(AuditRequestInfo requestInfo, String action, 
                           String actionType, String resourceType, Long resourceId,
                           Integer statusCode, Long responseTimeMs, String errorMessage) {
        try {
            AuditLog log = new AuditLog();
            log.setUser(null);
            log.setAction(action);
            log.setActionType(actionType);
            log.setResourceType(resourceType);
            log.setResourceId(resourceId);
            log.setRequestMethod(requestInfo.getMethod());
            log.setRequestUrl(requestInfo.getRequestUrl());
            log.setStatusCode(statusCode);
            log.setResponseTimeMs(responseTimeMs);
            log.setErrorMessage(errorMessage);
            
            // IP Address
            log.setIpAddress(requestInfo.getIpAddress());
            
            // User Agent
            log.setUserAgent(requestInfo.getUserAgent());
            parseUserAgent(log, requestInfo.getUserAgent());
            
            // Session
            log.setSessionId(requestInfo.getSessionId());
            
            // Headers
            log.setReferer(requestInfo.getReferer());
            log.setAcceptLanguage(requestInfo.getAcceptLanguage());
            
            // Paramètres
            log.setRequestParams(requestInfo.getQueryString());
            
            // Location
            detectLocation(log, requestInfo.getIpAddress());
            
            // Timestamp
            log.setTimestamp(LocalDateTime.now());
            
            auditLogRepository.save(log);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement de l'audit: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Récupère les logs avec filtres
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Long userId, String action, String actionType,
                                       String resourceType, String ipAddress, String countryCode,
                                       LocalDateTime startDate, LocalDateTime endDate,
                                       Pageable pageable) {
        Specification<AuditLog> spec = AuditLogSpecification.withFilters(
            userId, action, actionType, resourceType, ipAddress, countryCode,
            startDate, endDate
        );
        return auditLogRepository.findAll(spec, pageable);
    }
    
    /**
     * Récupère les statistiques d'audit
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAuditStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total de logs
        stats.put("totalLogs", auditLogRepository.count());
        
        // Logs des dernières 24h
        stats.put("recentLogs24h", auditLogRepository.countRecentLogs(LocalDateTime.now().minusDays(1)));
        
        // Actions les plus fréquentes
        stats.put("topActions", auditLogRepository.findMostFrequentActions(
            org.springframework.data.domain.PageRequest.of(0, 10)
        ));
        
        // Pays les plus actifs
        stats.put("topCountries", auditLogRepository.findMostActiveCountries(
            org.springframework.data.domain.PageRequest.of(0, 10)
        ));
        
        // Utilisateurs les plus actifs
        stats.put("topUsers", auditLogRepository.findMostActiveUsers(
            org.springframework.data.domain.PageRequest.of(0, 10)
        ));
        
        // IPs les plus actives
        stats.put("topIPs", auditLogRepository.findMostActiveIPs(
            org.springframework.data.domain.PageRequest.of(0, 10)
        ));
        
        // Statistiques quotidiennes
        stats.put("dailyStats", auditLogRepository.findDailyStatistics(30, 0));
        
        return stats;
    }
    
    /**
     * Nettoie les anciens logs (appelé périodiquement)
     */
    @Transactional
    public void cleanOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        auditLogRepository.deleteByTimestampBefore(cutoffDate);
    }
    
    /**
     * Nettoie les logs dans une période spécifique (date de début et date de fin)
     */
    @Transactional
    public void cleanLogsByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Les dates de début et de fin sont requises");
        }
        
        // S'assurer que endDate inclut la fin de journée
        if (endDate.getHour() == 0 && endDate.getMinute() == 0) {
            endDate = endDate.withHour(23).withMinute(59).withSecond(59);
        }
        
        // Supprimer les logs dans la période
        auditLogRepository.deleteByTimestampBetween(startDate, endDate);
    }
    
    /**
     * Récupère les utilisateurs actifs avec leurs statistiques d'audit
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getActiveUsersWithStats() {
        List<Object[]> topUsers = auditLogRepository.findMostActiveUsers(
            org.springframework.data.domain.PageRequest.of(0, 50)
        );
        
        List<Map<String, Object>> usersWithStats = new ArrayList<>();
        
        for (Object[] userData : topUsers) {
            Long userIdValue = (Long) userData[0];
            String email = (String) userData[1];
            Long logCount = (Long) userData[2];
            
            // Récupérer le dernier log de cet utilisateur
            Page<AuditLog> lastLogs = auditLogRepository.findByUserIdOrderByTimestampDesc(
                userIdValue, 
                org.springframework.data.domain.PageRequest.of(0, 1)
            );
            
            LocalDateTime lastActivity = null;
            String lastAction = null;
            String lastIp = null;
            if (!lastLogs.isEmpty()) {
                AuditLog lastLog = lastLogs.getContent().get(0);
                lastActivity = lastLog.getTimestamp();
                lastAction = lastLog.getAction();
                lastIp = lastLog.getIpAddress();
            }
            
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("userId", userIdValue);
            userStats.put("email", email);
            userStats.put("logCount", logCount);
            userStats.put("lastActivity", lastActivity);
            userStats.put("lastAction", lastAction);
            userStats.put("lastIp", lastIp);
            
            usersWithStats.add(userStats);
        }
        
        return usersWithStats;
    }
    
    /**
     * Récupère l'adresse IP réelle du client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
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
        
        // Si plusieurs IPs, prendre la première
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * Parse le User-Agent pour extraire les informations du navigateur et OS
     */
    private void parseUserAgent(AuditLog log, String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return;
        }
        
        // Détection du navigateur
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) {
            log.setBrowser("Chrome");
            Pattern pattern = Pattern.compile("Chrome/([\\d.]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                log.setBrowserVersion(matcher.group(1));
            }
        } else if (userAgent.contains("Firefox")) {
            log.setBrowser("Firefox");
            Pattern pattern = Pattern.compile("Firefox/([\\d.]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                log.setBrowserVersion(matcher.group(1));
            }
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            log.setBrowser("Safari");
            Pattern pattern = Pattern.compile("Version/([\\d.]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                log.setBrowserVersion(matcher.group(1));
            }
        } else if (userAgent.contains("Edg")) {
            log.setBrowser("Edge");
            Pattern pattern = Pattern.compile("Edg/([\\d.]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                log.setBrowserVersion(matcher.group(1));
            }
        } else if (userAgent.contains("Opera") || userAgent.contains("OPR")) {
            log.setBrowser("Opera");
        }
        
        // Détection de l'OS
        if (userAgent.contains("Windows")) {
            log.setOperatingSystem("Windows");
            if (userAgent.contains("Windows NT 10.0")) {
                log.setOperatingSystem("Windows 10/11");
            } else if (userAgent.contains("Windows NT 6.3")) {
                log.setOperatingSystem("Windows 8.1");
            } else if (userAgent.contains("Windows NT 6.2")) {
                log.setOperatingSystem("Windows 8");
            } else if (userAgent.contains("Windows NT 6.1")) {
                log.setOperatingSystem("Windows 7");
            }
        } else if (userAgent.contains("Mac OS X") || userAgent.contains("Macintosh")) {
            log.setOperatingSystem("macOS");
        } else if (userAgent.contains("Linux")) {
            log.setOperatingSystem("Linux");
        } else if (userAgent.contains("Android")) {
            log.setOperatingSystem("Android");
            log.setDeviceType("Mobile");
            log.setIsMobile(true);
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            log.setOperatingSystem("iOS");
            if (userAgent.contains("iPad")) {
                log.setDeviceType("Tablet");
            } else {
                log.setDeviceType("Mobile");
            }
            log.setIsMobile(true);
        }
        
        // Détection du type d'appareil si pas déjà défini
        if (log.getDeviceType() == null) {
            if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
                log.setDeviceType("Mobile");
                log.setIsMobile(true);
            } else if (userAgent.contains("Tablet") || userAgent.contains("iPad")) {
                log.setDeviceType("Tablet");
                log.setIsMobile(true);
            } else {
                log.setDeviceType("Desktop");
                log.setIsMobile(false);
            }
        }
        
        // Détection des bots
        String[] botPatterns = {"bot", "crawler", "spider", "scraper", "Googlebot", "Bingbot"};
        for (String pattern : botPatterns) {
            if (userAgent.toLowerCase().contains(pattern.toLowerCase())) {
                log.setIsBot(true);
                break;
            }
        }
        if (log.getIsBot() == null) {
            log.setIsBot(false);
        }
    }
    
    /**
     * Détecte la localisation basée sur l'IP (version simplifiée)
     * Note: Pour une détection précise, utiliser une API GeoIP comme MaxMind ou ipapi.co
     */
    private void detectLocation(AuditLog log, String ipAddress) {
        // Version simplifiée - dans un vrai projet, utiliser une API GeoIP
        // Ici on peut juste stocker l'IP et laisser une tâche asynchrone faire la géolocalisation
        
        // Pour l'instant, on peut détecter les IPs locales
        if (ipAddress != null) {
            if (ipAddress.startsWith("127.") || ipAddress.startsWith("192.168.") || 
                ipAddress.startsWith("10.") || ipAddress.equals("::1") || ipAddress.equals("localhost")) {
                log.setCountry("Local");
                log.setCountryCode("LOC");
                log.setCity("Local");
            }
            // Sinon, on pourrait appeler une API GeoIP ici
        }
    }
}

