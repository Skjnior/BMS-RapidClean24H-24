package com.rapidclean.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Page de login pour les employés
     */
    @GetMapping("/employee-login")
    public String employeeLogin() {
        return "employee-login";
    }

    /**
     * Traitement du login employé (authentification)
     */
    @PostMapping("/api/auth/employee-login")
    public String employeeLoginPost(@RequestParam String username,
                                     @RequestParam String password,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        try {
            // Créer le token d'authentification
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(username, password);
            
            // Authentifier
            Authentication auth = authenticationManager.authenticate(authToken);
            
            // Stocker le contexte de sécurité dans la session
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(auth);
            session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext
            );
            
            // Redirection vers le dashboard (l'interceptor se chargera du reste)
            return "redirect:/employee/dashboard";
            
        } catch (AuthenticationException e) {
            redirectAttributes.addFlashAttribute("error", "Email ou mot de passe incorrect");
            return "redirect:/employee-login";
        }
    }

    /**
     * Page de login pour les administrateurs
     */
    @GetMapping("/admin-login")
    public String adminLogin() {
        return "admin/login";
    }
    
    /**
     * Page de choix de connexion (employé ou admin)
     */
    @GetMapping("/login-choice")
    public String loginChoice() {
        return "login-choice";
    }
}
