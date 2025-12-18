package com.rapidclean.controller;

import com.rapidclean.entity.AuditLog;
import com.rapidclean.repository.AuditLogRepository;
import com.rapidclean.repository.UserRepository;
import com.rapidclean.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminAuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/audit")
    public String auditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Model model) {
        
        try {
            // Parse dates if provided
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (startDate != null && !startDate.isEmpty()) {
                startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            }
            if (endDate != null && !endDate.isEmpty()) {
                endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            }
            
            // Create pageable
            Pageable pageable = PageRequest.of(page, size);
            
            // Get audit logs with filters
            Page<AuditLog> auditLogs = auditService.getAuditLogs(
                userId, action, actionType, resourceType, ipAddress, countryCode,
                startDateTime, endDateTime, pageable
            );
            
            // Get statistics
            var stats = auditService.getAuditStatistics();
            
            // Get users for filter dropdown
            var users = userRepository.findAll();
            
            model.addAttribute("pageTitle", "Logs d'Audit");
            model.addAttribute("pageDescription", "Consultation des logs d'audit du système");
            model.addAttribute("auditLogs", auditLogs);
            model.addAttribute("stats", stats);
            model.addAttribute("users", users);
            
            // Filter parameters
            model.addAttribute("filterUserId", userId);
            model.addAttribute("filterAction", action);
            model.addAttribute("filterActionType", actionType);
            model.addAttribute("filterResourceType", resourceType);
            model.addAttribute("filterIpAddress", ipAddress);
            model.addAttribute("filterCountryCode", countryCode);
            model.addAttribute("filterStartDate", startDate);
            model.addAttribute("filterEndDate", endDate);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", auditLogs.getTotalPages());
            model.addAttribute("totalItems", auditLogs.getTotalElements());
            
            return "admin/audit";
            
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des logs d'audit: " + e.getMessage());
            return "admin/audit";
        }
    }

    @GetMapping("/audit/{id}")
    public String auditLogDetail(@PathVariable Long id, Model model) {
        try {
            AuditLog log = auditLogRepository.findByIdWithUser(id)
                .orElseThrow(() -> new RuntimeException("Log d'audit non trouvé"));
            
            model.addAttribute("pageTitle", "Détail du Log d'Audit");
            model.addAttribute("pageDescription", "Détails du log d'audit #" + id);
            model.addAttribute("log", log);
            
            return "admin/audit-detail";
            
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement du log: " + e.getMessage());
            return "redirect:/admin/audit";
        }
    }

    @PostMapping("/audit/delete")
    @Transactional
    public String deleteAuditLogs(
            @RequestParam(value = "logIds") String[] logIds,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (logIds == null || logIds.length == 0) {
                redirectAttributes.addFlashAttribute("error", "Aucun log sélectionné pour la suppression.");
                return buildRedirectUrl(userId, action, actionType, resourceType, ipAddress, countryCode, startDate, endDate, page, size);
            }
            
            // Convertir le tableau en liste de Long
            List<Long> ids = Arrays.stream(logIds)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            
            if (ids.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Aucun log sélectionné pour la suppression.");
                return buildRedirectUrl(userId, action, actionType, resourceType, ipAddress, countryCode, startDate, endDate, page, size);
            }
            
            // Supprimer les logs
            auditLogRepository.deleteByIds(ids);
            
            redirectAttributes.addFlashAttribute("success", ids.size() + " log(s) d'audit supprimé(s) avec succès.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression des logs: " + e.getMessage());
        }
        
        return buildRedirectUrl(userId, action, actionType, resourceType, ipAddress, countryCode, startDate, endDate, page, size);
    }
    
    private String buildRedirectUrl(Long userId, String action, String actionType, String resourceType,
                                   String ipAddress, String countryCode, String startDate, String endDate,
                                   int page, int size) {
        StringBuilder url = new StringBuilder("redirect:/admin/audit?");
        
        if (userId != null) url.append("userId=").append(userId).append("&");
        if (action != null && !action.isEmpty()) url.append("action=").append(action).append("&");
        if (actionType != null && !actionType.isEmpty()) url.append("actionType=").append(actionType).append("&");
        if (resourceType != null && !resourceType.isEmpty()) url.append("resourceType=").append(resourceType).append("&");
        if (ipAddress != null && !ipAddress.isEmpty()) url.append("ipAddress=").append(ipAddress).append("&");
        if (countryCode != null && !countryCode.isEmpty()) url.append("countryCode=").append(countryCode).append("&");
        if (startDate != null && !startDate.isEmpty()) url.append("startDate=").append(startDate).append("&");
        if (endDate != null && !endDate.isEmpty()) url.append("endDate=").append(endDate).append("&");
        
        url.append("page=").append(page).append("&");
        url.append("size=").append(size);
        
        return url.toString();
    }
}

