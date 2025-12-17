package com.rapidclean.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.rapidclean.entity.User;
import com.rapidclean.repository.UserRepository;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.rapidclean.security.SecurityConfig securityConfig;
    
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return securityConfig.passwordEncoder();
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Email ou mot de passe incorrect");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(User user, RedirectAttributes redirectAttributes) {
        try {
            if (userRepository.existsByEmail(user.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Cet email est déjà utilisé");
                return "redirect:/register";
            }

            user.setPassword(passwordEncoder().encode(user.getPassword()));
            user.setRole(User.Role.CLIENT);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Compte créé avec succès! Vous pouvez maintenant vous connecter.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création du compte");
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        
        if (user != null && user.getRole() == User.Role.ADMIN) {
            return "redirect:/admin/dashboard";
        }
        
        return "redirect:/admin-secret-access";
    }
}
