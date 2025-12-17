package com.rapidclean.controller;

import com.rapidclean.entity.AuditLog;
import com.rapidclean.entity.User;
import com.rapidclean.repository.AuditLogRepository;
import com.rapidclean.repository.AuditLogSpecification;
import com.rapidclean.repository.UserRepository;
import com.rapidclean.service.AuditService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/audit")
public class AdminAuditController {
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Page principale des logs d'audit avec filtres
     */
    @GetMapping
    public String auditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Model model) {
        
        // Créer la pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        
        // Récupérer les logs filtrés
        Page<AuditLog> auditLogs = auditService.getAuditLogs(
            userId, action, actionType, resourceType, ipAddress, countryCode,
            startDate, endDate, pageable
        );
        
        // Récupérer les statistiques
        Map<String, Object> statistics = auditService.getAuditStatistics();
        
        // Récupérer les listes pour les filtres
        List<User> allUsers = userRepository.findAll();
        
        // Récupérer les utilisateurs actifs avec leurs statistiques
        List<Map<String, Object>> activeUsersWithStats = auditService.getActiveUsersWithStats();
        
        // Enrichir avec les informations utilisateur complètes
        for (Map<String, Object> userStats : activeUsersWithStats) {
            Long userIdValue = (Long) userStats.get("userId");
            if (userIdValue != null) {
                User user = userRepository.findById(userIdValue).orElse(null);
                if (user != null) {
                    userStats.put("user", user);
                    userStats.put("fullName", user.getFullName());
                    userStats.put("role", user.getRole());
                    userStats.put("enabled", user.isEnabled());
                }
            }
        }
        
        // Ajouter les attributs au modèle
        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("statistics", statistics);
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("activeUsersWithStats", activeUsersWithStats);
        model.addAttribute("pageTitle", "Journal d'Audit");
        model.addAttribute("pageDescription", "Suivi complet de toutes les activités du système");
        
        // Paramètres de filtre pour les garder dans le formulaire
        model.addAttribute("filterUserId", userId);
        model.addAttribute("filterAction", action);
        model.addAttribute("filterActionType", actionType);
        model.addAttribute("filterResourceType", resourceType);
        model.addAttribute("filterIpAddress", ipAddress);
        model.addAttribute("filterCountryCode", countryCode);
        model.addAttribute("filterStartDate", startDate);
        model.addAttribute("filterEndDate", endDate);
        
        // Actions disponibles pour le filtre
        model.addAttribute("availableActions", getAvailableActions());
        model.addAttribute("availableActionTypes", getAvailableActionTypes());
        model.addAttribute("availableResourceTypes", getAvailableResourceTypes());
        
        return "admin/audit";
    }
    
    /**
     * Détails d'un log d'audit spécifique
     */
    @GetMapping("/{id}")
    public String auditLogDetail(@PathVariable Long id, Model model) {
        AuditLog log = auditLogRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Log d'audit non trouvé"));
        
        model.addAttribute("log", log);
        model.addAttribute("pageTitle", "Détails du Log d'Audit");
        model.addAttribute("pageDescription", "Informations détaillées sur cette activité");
        
        return "admin/audit-detail";
    }
    
    /**
     * Export des logs en CSV
     */
    @GetMapping("/export")
    public void exportAuditLogs(
            @RequestParam(required = false) String format,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String startDateStr,
            @RequestParam(required = false) String endDateStr,
            HttpServletResponse response) throws IOException {
        
        // Parser les dates depuis le format HTML (yyyy-MM-ddTHH:mm) ou ISO
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        
        if (startDateStr != null && !startDateStr.isEmpty()) {
            try {
                String dateStr = startDateStr.replace(" ", "T");
                // Si le format est yyyy-MM-ddTHH:mm (sans secondes), ajouter :00
                if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")) {
                    dateStr += ":00";
                }
                startDate = LocalDateTime.parse(dateStr);
            } catch (Exception e) {
                // Si le parsing échoue, ignorer la date
                System.err.println("Erreur parsing startDate: " + e.getMessage());
            }
        }
        
        if (endDateStr != null && !endDateStr.isEmpty()) {
            try {
                String dateStr = endDateStr.replace(" ", "T");
                // Si le format est yyyy-MM-ddTHH:mm (sans secondes), ajouter :59:59 pour la fin de journée
                if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")) {
                    dateStr += ":59";
                }
                // Si pas de secondes du tout, ajouter :59:59
                if (!dateStr.contains(":")) {
                    dateStr += "T23:59:59";
                } else if (dateStr.split(":").length == 2) {
                    dateStr += ":59";
                }
                endDate = LocalDateTime.parse(dateStr);
            } catch (Exception e) {
                // Si le parsing échoue, ignorer la date
                System.err.println("Erreur parsing endDate: " + e.getMessage());
            }
        }
        
        // Récupérer tous les logs correspondant aux filtres (sans pagination)
        Specification<AuditLog> spec = AuditLogSpecification.withFilters(
            userId, action, actionType, resourceType, ipAddress, countryCode,
            startDate, endDate
        );
        
        List<AuditLog> allLogs = auditLogRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "timestamp"));
        
        // Configurer la réponse HTTP
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Nom du fichier avec timestamp
        String filename = "audit_logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        
        // Ajouter BOM pour Excel UTF-8
        response.getWriter().print('\ufeff');
        
        // Écrire le CSV
        try (PrintWriter writer = response.getWriter()) {
            // En-têtes CSV
            writer.println("ID,Date/Heure,Utilisateur,Email,Rôle,Action,Type Action,Type Ressource,ID Ressource,Méthode HTTP,URL,Adresse IP,Pays,Code Pays,Ville,Navigateur,Version Navigateur,Système d'Exploitation,Type Appareil,Mobile,Bot,Temps Réponse (ms),Code Statut,Session ID,Referer,Accept Language,Erreur");
            
            // Données
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            for (AuditLog log : allLogs) {
                writer.print(escapeCsv(log.getId() != null ? log.getId().toString() : ""));
                writer.print(",");
                writer.print(escapeCsv(log.getTimestamp() != null ? log.getTimestamp().format(dateFormatter) : ""));
                writer.print(",");
                writer.print(escapeCsv(log.getUser() != null ? log.getUser().getFullName() : "Anonyme"));
                writer.print(",");
                writer.print(escapeCsv(log.getUser() != null ? log.getUser().getEmail() : ""));
                writer.print(",");
                writer.print(escapeCsv(log.getUser() != null && log.getUser().getRole() != null ? log.getUser().getRole().name() : ""));
                writer.print(",");
                writer.print(escapeCsv(log.getAction()));
                writer.print(",");
                writer.print(escapeCsv(log.getActionType()));
                writer.print(",");
                writer.print(escapeCsv(log.getResourceType()));
                writer.print(",");
                writer.print(escapeCsv(log.getResourceId() != null ? log.getResourceId().toString() : ""));
                writer.print(",");
                writer.print(escapeCsv(log.getRequestMethod()));
                writer.print(",");
                writer.print(escapeCsv(log.getRequestUrl()));
                writer.print(",");
                writer.print(escapeCsv(log.getIpAddress()));
                writer.print(",");
                writer.print(escapeCsv(log.getCountry()));
                writer.print(",");
                writer.print(escapeCsv(log.getCountryCode()));
                writer.print(",");
                writer.print(escapeCsv(log.getCity()));
                writer.print(",");
                writer.print(escapeCsv(log.getBrowser()));
                writer.print(",");
                writer.print(escapeCsv(log.getBrowserVersion()));
                writer.print(",");
                writer.print(escapeCsv(log.getOperatingSystem()));
                writer.print(",");
                writer.print(escapeCsv(log.getDeviceType()));
                writer.print(",");
                writer.print(escapeCsv(log.getIsMobile() != null ? log.getIsMobile().toString() : ""));
                writer.print(",");
                writer.print(escapeCsv(log.getIsBot() != null ? log.getIsBot().toString() : ""));
                writer.print(",");
                writer.print(escapeCsv(log.getResponseTimeMs() != null ? log.getResponseTimeMs().toString() : ""));
                writer.print(",");
                writer.print(escapeCsv(log.getStatusCode() != null ? log.getStatusCode().toString() : ""));
                writer.print(",");
                writer.print(escapeCsv(log.getSessionId()));
                writer.print(",");
                writer.print(escapeCsv(log.getReferer()));
                writer.print(",");
                writer.print(escapeCsv(log.getAcceptLanguage()));
                writer.print(",");
                writer.print(escapeCsv(log.getErrorMessage()));
                writer.println();
            }
            
            writer.flush();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'export CSV: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Échappe les valeurs CSV (gère les guillemets et les virgules)
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Si la valeur contient des guillemets, des virgules ou des retours à la ligne, l'entourer de guillemets
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            // Remplacer les guillemets par double guillemets
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * Nettoyer les anciens logs
     */
    @PostMapping("/cleanup")
    public String cleanupOldLogs(@RequestParam(defaultValue = "90") int daysToKeep,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Compter les logs avant suppression
            long countBefore = auditLogRepository.count();
            
            // Supprimer les logs
            auditService.cleanOldLogs(daysToKeep);
            
            long countAfter = auditLogRepository.count();
            long deletedCount = countBefore - countAfter;
            
            redirectAttributes.addFlashAttribute("success", 
                deletedCount + " log(s) de plus de " + daysToKeep + " jour(s) ont été supprimé(s) avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors du nettoyage des logs: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/audit";
    }
    
    /**
     * Nettoyer les logs par période (date de début et date de fin)
     */
    @PostMapping("/cleanup-by-period")
    public String cleanupByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            RedirectAttributes redirectAttributes) {
        try {
            // Convertir LocalDate en LocalDateTime
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            // Compter les logs avant suppression
            long countBefore = auditLogRepository.count();
            
            // Supprimer les logs dans la période
            auditService.cleanLogsByPeriod(startDateTime, endDateTime);
            
            long countAfter = auditLogRepository.count();
            long deletedCount = countBefore - countAfter;
            
            String startDateStr = startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String endDateStr = endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            redirectAttributes.addFlashAttribute("success", 
                deletedCount + " log(s) du " + startDateStr + " au " + endDateStr + " ont été supprimé(s) avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors du nettoyage des logs: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/audit";
    }
    
    /**
     * API pour obtenir les statistiques en JSON
     */
    @GetMapping("/api/statistics")
    @ResponseBody
    public Map<String, Object> getStatistics() {
        return auditService.getAuditStatistics();
    }
    
    // Méthodes helper pour les listes de filtres
    private List<String> getAvailableActions() {
        return List.of(
            "LOGIN", "LOGOUT", "REGISTER", "VIEW", "VIEW_DASHBOARD", "VIEW_DETAIL",
            "CREATE", "UPDATE", "DELETE", "POST", "GET", "PUT", "PATCH"
        );
    }
    
    private List<String> getAvailableActionTypes() {
        return List.of(
            "AUTHENTICATION", "VIEW", "MODIFICATION", "API_CALL", "GENERAL"
        );
    }
    
    private List<String> getAvailableResourceTypes() {
        return List.of(
            "User", "Service", "ServiceRequest", "Message", "Review", 
            "Notification", "TimeTracking", "Absence", "WorkplaceObservation", 
            "AuditLog", "Dashboard", "Unknown"
        );
    }
}

