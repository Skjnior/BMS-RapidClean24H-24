package com.rapidclean.controller;

import com.rapidclean.entity.ServiceRequest;
import com.rapidclean.entity.User;
import com.rapidclean.entity.Notification;
import com.rapidclean.repository.ServiceRequestRepository;
import com.rapidclean.repository.UserRepository;
import com.rapidclean.repository.NotificationRepository;
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

    @GetMapping("/request-service")
    public String requestForm(Model model) {
        model.addAttribute("serviceRequest", new ServiceRequest());
        return "request-form";
    }

    @PostMapping("/request-service")
    public String submitRequest(@ModelAttribute ServiceRequest serviceRequest,
                              @RequestParam String clientName,
                              @RequestParam String clientEmail,
                              @RequestParam String clientPhone,
                              RedirectAttributes redirectAttributes) {
        try {
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
            
            ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

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

            redirectAttributes.addFlashAttribute("success", 
                "Votre demande a été envoyée avec succès ! Nous vous contacterons bientôt.");
            
            return "redirect:/";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Une erreur est survenue. Veuillez réessayer.");
            return "redirect:/request-service";
        }
    }
}
