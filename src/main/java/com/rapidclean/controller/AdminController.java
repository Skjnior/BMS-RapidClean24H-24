package com.rapidclean.controller;

import com.rapidclean.entity.ContactMessage;
import com.rapidclean.entity.Notification;
import com.rapidclean.entity.Review;
import com.rapidclean.entity.Service;
import com.rapidclean.entity.ServiceRequest;
import com.rapidclean.entity.User;
import com.rapidclean.repository.*;
import com.rapidclean.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<ServiceRequest> requests = serviceRequestRepository.findAll();
        List<Service> services = serviceRepository.findAll();
        List<User> users = userRepository.findAll();
        List<ContactMessage> messages = contactMessageRepository.findAll();
        List<Review> reviews = reviewRepository.findAll();
        List<Notification> notifications = notificationRepository.findByReadFalseOrderByCreatedAtDesc();
        
        model.addAttribute("totalRequests", requests.size());
        model.addAttribute("totalServices", services.size());
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalMessages", messages.size());
        model.addAttribute("totalReviews", reviews.size());
        model.addAttribute("pendingRequests", serviceRequestRepository.countByStatus(ServiceRequest.Status.PENDING));
        model.addAttribute("unreadMessages", contactMessageRepository.countUnreadMessages());
        model.addAttribute("pendingReviews", reviewRepository.findByApprovedFalseOrderByCreatedAtDesc().size());
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotifications", notificationRepository.countUnreadNotifications());
        
        return "admin/dashboard";
    }

    @GetMapping("/services")
    public String services(Model model) {
        List<Service> services = serviceRepository.findAll();
        model.addAttribute("services", services);
        return "admin/services";
    }

    @GetMapping("/services/new")
    public String newService(Model model) {
        model.addAttribute("service", new Service());
        return "admin/service-form";
    }

    @PostMapping("/services")
    public String saveService(Service service, RedirectAttributes redirectAttributes) {
        try {
            serviceRepository.save(service);
            redirectAttributes.addFlashAttribute("success", "Service créé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création du service");
        }
        return "redirect:/admin/services";
    }

    @GetMapping("/requests")
    public String requests(Model model) {
        List<ServiceRequest> requests = serviceRequestRepository.findAll();
        model.addAttribute("requests", requests);
        return "admin/requests";
    }

    @PostMapping("/requests/{id}/status")
    public String updateRequestStatus(@PathVariable Long id, @RequestParam ServiceRequest.Status status, 
                                    RedirectAttributes redirectAttributes) {
        try {
            ServiceRequest request = serviceRequestRepository.findById(id).orElse(null);
            if (request != null) {
                request.setStatus(status);
                serviceRequestRepository.save(request);
                redirectAttributes.addFlashAttribute("success", "Statut mis à jour avec succès");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour");
        }
        return "redirect:/admin/requests";
    }

    @GetMapping("/users")
    public String users(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }
}
