package com.rapidclean.controller;

import com.rapidclean.entity.ServiceRequest;
import com.rapidclean.entity.User;
import com.rapidclean.entity.Notification;
import com.rapidclean.repository.ServiceRequestRepository;
import com.rapidclean.repository.UserRepository;
import com.rapidclean.repository.NotificationRepository;
import com.rapidclean.service.RateLimitingService;
import com.rapidclean.service.SpamProtectionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class ServiceRequestController {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private SpamProtectionService spamProtectionService;

    @GetMapping("/request-service")
    public String requestForm(Model model) {
        model.addAttribute("serviceRequest", new ServiceRequest());
        return "request-form";
    }

    @PostMapping("/request-service")
    public String submitRequest(
            @ModelAttribute ServiceRequest serviceRequest,
            @RequestParam String clientName,
            @RequestParam String clientEmail,
            @RequestParam String clientPhone,
            @RequestParam(value = "website", required = false) String honeypot,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            // Vérification honeypot
            if (!spamProtectionService.isHoneypotValid(honeypot)) {
                redirectAttributes.addFlashAttribute("error", "Requête invalide.");
                return "redirect:/request-service";
            }
            
            // Vérification rate limiting
            String clientIp = getClientIp(request);
            String identifier = clientIp + "_request_" + clientEmail;
            
            if (!rateLimitingService.isAllowed(identifier)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Trop de requêtes. Veuillez patienter quelques minutes avant de réessayer.");
                return "redirect:/request-service";
            }
            
            // Vérification spam
            String fullContent = serviceRequest.getDescription() != null ? serviceRequest.getDescription() : "";
            if (spamProtectionService.isSpam(fullContent, clientEmail)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Votre demande n'a pas pu être envoyée. Veuillez vérifier le contenu.");
                return "redirect:/request-service";
            }
            // Créer un utilisateur client temporaire ou utiliser un existant
            User client = userRepository.findByEmail(clientEmail).orElse(null);
            
            if (client == null) {
                // Créer un nouveau client
                client = new User();
                client.setFirstName(clientName.split(" ")[0]);
                client.setLastName(clientName.split(" ").length > 1 ? 
                    clientName.substring(clientName.indexOf(" ") + 1) : "");
                client.setEmail(clientEmail);
                client.setPhone(clientPhone);
                client.setRole(User.Role.CLIENT);
                client.setPassword("temp123"); // Mot de passe temporaire
                client = userRepository.save(client);
            }

            // Créer la demande de service
            serviceRequest.setUser(client);
            serviceRequest.setStatus(ServiceRequest.Status.PENDING);
            serviceRequest.setCreatedAt(LocalDateTime.now());
            serviceRequest.setUpdatedAt(LocalDateTime.now());
            
            serviceRequestRepository.save(serviceRequest);

            // Créer une notification pour l'admin
            Notification notification = new Notification();
            notification.setTitle("Nouvelle demande de service");
            notification.setMessage("Une nouvelle demande de service a été soumise par " + clientName + 
                                 " pour " + serviceRequest.getServiceType() + " le " + 
                                 serviceRequest.getServiceDate().toLocalDate());
            notification.setType(Notification.Type.NEW_REQUEST);
            notification.setPriority(Notification.Priority.MEDIUM);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(notification);

            // Réinitialiser le rate limiting après succès
            rateLimitingService.reset(identifier);

            redirectAttributes.addFlashAttribute("success", 
                "Demande envoyée avec succès ! Nous avons bien reçu votre demande et nous vous contacterons dans les plus brefs délais.");
            redirectAttributes.addFlashAttribute("redirectToHome", true);
            
            return "redirect:/request-service";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Une erreur est survenue. Veuillez réessayer.");
            return "redirect:/request-service";
        }
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
