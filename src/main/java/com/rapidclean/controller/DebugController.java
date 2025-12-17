package com.rapidclean.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rapidclean.entity.User;
import com.rapidclean.repository.UserRepository;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.rapidclean.security.SecurityConfig securityConfig;
    
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return securityConfig.passwordEncoder();
    }

    /**
     * Endpoint de diagnostic pour vérifier l'état de l'admin
     * URL: http://localhost:8997/api/debug/admin-status
     */
    @GetMapping("/admin-status")
    public Map<String, Object> checkAdminStatus() {
        Map<String, Object> response = new HashMap<>();
        
        Optional<User> adminOpt = userRepository.findByEmail("admin@bmsrapidclean.com");
        
        if (adminOpt.isEmpty()) {
            response.put("status", "NOT_FOUND");
            response.put("message", "Admin non trouvé en base de données");
            return response;
        }
        
        User admin = adminOpt.get();
        response.put("status", "FOUND");
        response.put("email", admin.getEmail());
        response.put("firstName", admin.getFirstName());
        response.put("lastName", admin.getLastName());
        response.put("role", admin.getRole());
        response.put("enabled", admin.isEnabled());
        response.put("firstLogin", admin.isFirstLogin());
        response.put("createdAt", admin.getCreatedAt());
        response.put("password_hash", admin.getPassword().substring(0, 20) + "...");
        response.put("password_correct", passwordEncoder().matches("admin123", admin.getPassword()));
        
        return response;
    }

    /**
     * Endpoint pour réinitialiser l'admin en cas d'urgence
     * URL: POST http://localhost:8997/api/debug/reset-admin
     * 
     * ⚠️ À UTILISER UNIQUEMENT EN CAS DE PROBLÈME
     * Réinitialise le mot de passe à : admin123
     */
    @PostMapping("/reset-admin")
    public Map<String, Object> resetAdmin() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> adminOpt = userRepository.findByEmail("admin@bmsrapidclean.com");
            
            if (adminOpt.isEmpty()) {
                // Créer un nouvel admin
                User newAdmin = new User();
                newAdmin.setFirstName("Admin");
                newAdmin.setLastName("System");
                newAdmin.setEmail("admin@bmsrapidclean.com");
                newAdmin.setPassword(passwordEncoder().encode("admin123"));
                newAdmin.setPhone("+1 (555) 000-0000");
                newAdmin.setRole(User.Role.ADMIN);
                newAdmin.setEnabled(true);
                newAdmin.setFirstLogin(false);
                
                userRepository.save(newAdmin);
                
                response.put("status", "CREATED");
                response.put("message", "Admin créé avec succès");
            } else {
                // Réinitialiser le mot de passe
                User admin = adminOpt.get();
                admin.setPassword(passwordEncoder().encode("admin123"));
                admin.setFirstLogin(false);
                admin.setEnabled(true);
                admin.setRole(User.Role.ADMIN);
                
                userRepository.save(admin);
                
                response.put("status", "RESET");
                response.put("message", "Admin réinitialisé avec succès");
            }
            
            response.put("email", "admin@bmsrapidclean.com");
            response.put("password", "admin123");
            response.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        }
        
        return response;
    }
}
