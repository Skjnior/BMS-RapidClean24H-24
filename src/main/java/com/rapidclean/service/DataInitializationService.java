package com.rapidclean.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.rapidclean.entity.User;
import com.rapidclean.repository.UserRepository;

@Service
@Order(1)
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.rapidclean.security.SecurityConfig securityConfig;
    
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return securityConfig.passwordEncoder();
    }

    @Override
    public void run(String... args) throws Exception {
        // Créer l'utilisateur admin par défaut UNIQUEMENT s'il n'existe pas déjà
        // Ne pas le réinitialiser s'il existe déjà pour éviter d'écraser les modifications
        User admin = userRepository.findByEmail("admin@bmsrapidclean.com").orElse(null);
        
        if (admin == null) {
            // Créer un nouvel admin uniquement s'il n'existe pas
            admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("System");
            admin.setEmail("admin@bmsrapidclean.com");
            admin.setPassword(passwordEncoder().encode("admin123"));
            admin.setPhone("+1 (555) 000-0000");
            admin.setRole(User.Role.ADMIN);
            admin.setEnabled(true);
            admin.setFirstLogin(false);
            
            userRepository.save(admin);
            System.out.println("✅ Utilisateur admin créé: admin@bmsrapidclean.com / admin123");
        } else {
            // L'admin existe déjà - ne pas le modifier pour éviter d'écraser les changements
            System.out.println("ℹ️  Utilisateur admin existant trouvé: admin@bmsrapidclean.com (non modifié)");
        }

        // Note: Interface client désactivée pour l'instant
        // Seul l'administrateur est créé
    }
}
