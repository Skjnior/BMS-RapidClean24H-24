package com.rapidclean.service;

import com.rapidclean.entity.User;
import com.rapidclean.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Order(1)
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Créer l'utilisateur admin par défaut s'il n'existe pas
        if (!userRepository.existsByEmail("admin@bmsrapidclean.com")) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("System");
            admin.setEmail("admin@bmsrapidclean.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setPhone("+1 (555) 000-0000");
            admin.setRole(User.Role.ADMIN);
            admin.setEnabled(true);
            
            userRepository.save(admin);
            System.out.println("✅ Utilisateur admin créé: admin@bmsrapidclean.com / admin123");
        }

        // Note: Interface client désactivée pour l'instant
        // Seul l'administrateur est créé
    }
}
