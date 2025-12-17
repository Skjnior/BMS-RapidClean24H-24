package com.rapidclean.controller;

import com.rapidclean.entity.ContactMessage;
import com.rapidclean.repository.ContactMessageRepository;
import com.rapidclean.service.NotificationService;
import com.rapidclean.service.RateLimitingService;
import com.rapidclean.service.SpamProtectionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class ContactController {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private SpamProtectionService spamProtectionService;

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("contactMessage", new ContactMessage());
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(
            ContactMessage contactMessage,
            @RequestParam(value = "website", required = false) String honeypot,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Vérification honeypot (anti-spam)
            if (!spamProtectionService.isHoneypotValid(honeypot)) {
                redirectAttributes.addFlashAttribute("error", "Requête invalide.");
                return "redirect:/contact";
            }
            
            // Vérification rate limiting
            String clientIp = getClientIp(request);
            String identifier = clientIp + "_" + (contactMessage.getEmail() != null ? contactMessage.getEmail() : "");
            
            if (!rateLimitingService.isAllowed(identifier)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Trop de requêtes. Veuillez patienter quelques minutes avant de réessayer.");
                return "redirect:/contact";
            }
            
            // Vérification spam
            String fullContent = contactMessage.getSubject() + " " + 
                               (contactMessage.getMessage() != null ? contactMessage.getMessage() : "");
            
            if (spamProtectionService.isSpam(fullContent, contactMessage.getEmail())) {
                redirectAttributes.addFlashAttribute("error", 
                    "Votre message n'a pas pu être envoyé. Veuillez vérifier le contenu.");
                return "redirect:/contact";
            }
            
            contactMessageRepository.save(contactMessage);
            
            // Créer une notification pour l'admin
            notificationService.createNotification(
                "Nouveau Message de Contact",
                "Un nouveau message a été reçu de " + contactMessage.getFullName() + " - " + contactMessage.getSubject(),
                com.rapidclean.entity.Notification.Type.NEW_MESSAGE,
                com.rapidclean.entity.Notification.Priority.HIGH
            );
            
            // Réinitialiser le rate limiting après succès
            rateLimitingService.reset(identifier);
            
            redirectAttributes.addFlashAttribute("success", 
                "✅ Message envoyé avec succès ! Nous avons bien reçu votre message et nous vous répondrons dans les plus brefs délais.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'envoi du message");
        }
        return "redirect:/contact";
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
