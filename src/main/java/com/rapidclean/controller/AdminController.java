package com.rapidclean.controller;

import com.rapidclean.entity.ContactMessage;
import com.rapidclean.entity.Notification;
import com.rapidclean.entity.Review;
import com.rapidclean.entity.Service;
import com.rapidclean.entity.ServiceRequest;
import com.rapidclean.entity.User;
import com.rapidclean.entity.TimeTracking;
import com.rapidclean.entity.Absence;
import com.rapidclean.entity.WorkplaceObservation;
import com.rapidclean.repository.*;
import com.rapidclean.service.FileStorageService;
import com.rapidclean.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import java.security.Principal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private TimeTrackingRepository timeTrackingRepository;

    @Autowired
    private AbsenceRepository absenceRepository;

    @Autowired
    private WorkplaceObservationRepository workplaceObservationRepository;

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private BackupService backupService;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @ModelAttribute
    public void addAdminCommonAttributes(Model model) {
        try {
            long totalEmployees = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == User.Role.EMPLOYEE)
                    .count();
            model.addAttribute("totalEmployees", totalEmployees);
            
            // Add unread notifications count to all admin pages
            long unreadNotifications = notificationRepository.countUnreadNotifications();
            model.addAttribute("unreadNotifications", unreadNotifications);
            
            // Add other common stats
            model.addAttribute("totalServices", serviceRepository.count());
            model.addAttribute("totalUsers", userRepository.count());
            model.addAttribute("unreadMessages", contactMessageRepository.countUnreadMessages());
            model.addAttribute("pendingReviews", reviewRepository.findAll().stream().filter(r -> !r.isApproved()).count());
            model.addAttribute("pendingRequests", serviceRequestRepository.countByStatus(ServiceRequest.Status.PENDING));
        } catch (Exception e) {
            model.addAttribute("totalEmployees", 0);
            model.addAttribute("unreadNotifications", 0);
            model.addAttribute("totalServices", 0);
            model.addAttribute("totalUsers", 0);
            model.addAttribute("unreadMessages", 0);
            model.addAttribute("pendingReviews", 0);
            model.addAttribute("pendingRequests", 0);
        }
    }


   @GetMapping("/dashboard")
public String dashboard(Principal principal, Model model) {
    List<ServiceRequest> requests = serviceRequestRepository.findAll();
    List<Service> services = serviceRepository.findAll();
    List<User> users = userRepository.findAll();
    List<ContactMessage> messages = contactMessageRepository.findAll();
    List<Review> reviews = reviewRepository.findAll();
    List<Notification> notifications = notificationRepository.findByReadFalseOrderByCreatedAtDesc();

    // --- STATUTS DES DEMANDES (manquants) ---
    long pendingRequests   = serviceRequestRepository.countByStatus(ServiceRequest.Status.PENDING);
    long confirmedRequests = serviceRequestRepository.countByStatus(ServiceRequest.Status.CONFIRMED);
    long inProgressRequests= serviceRequestRepository.countByStatus(ServiceRequest.Status.IN_PROGRESS);
    long completedRequests = serviceRequestRepository.countByStatus(ServiceRequest.Status.COMPLETED);

    model.addAttribute("pageTitle", "Dashboard Administrateur");
    model.addAttribute("pageDescription", "Vue d'ensemble de la plateforme BMS Rapid Clean");

    model.addAttribute("totalRequests", requests.size());
    model.addAttribute("totalServices", services.size());
    model.addAttribute("totalUsers", users.size());
    long totalEmployees = users.stream().filter(u -> u.getRole() == User.Role.EMPLOYEE).count();
    model.addAttribute("totalEmployees", totalEmployees);
    model.addAttribute("totalMessages", messages.size());
    model.addAttribute("totalReviews", reviews.size());

    model.addAttribute("pendingRequests", pendingRequests);
    model.addAttribute("confirmedRequests", confirmedRequests);
    model.addAttribute("inProgressRequests", inProgressRequests);
    model.addAttribute("completedRequests", completedRequests);

    model.addAttribute("unreadMessages", contactMessageRepository.countUnreadMessages());
    
    // --- STATISTIQUES DES AVIS ---
    long pendingReviews = reviewRepository.findByApprovedFalseOrderByCreatedAtDesc().size();
    long approvedReviews = reviewRepository.findByApprovedTrueOrderByCreatedAtDesc().size();
    
    // Calcul de la note moyenne (seulement pour les avis approuvés)
    double averageRatingValue = 0.0;
    List<Review> approvedReviewsList = reviewRepository.findByApprovedTrueOrderByCreatedAtDesc();
    if (!approvedReviewsList.isEmpty()) {
        averageRatingValue = approvedReviewsList.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
    
    model.addAttribute("pendingReviews", pendingReviews);
    model.addAttribute("approvedReviews", approvedReviews);
    // Format avec 1 décimale, ou "0.0" si aucun avis approuvé
    model.addAttribute("averageRating", approvedReviews > 0 ? String.format("%.1f", averageRatingValue) : "0.0");
    
    model.addAttribute("notifications", notifications);
    model.addAttribute("unreadNotifications", notificationRepository.countUnreadNotifications());

    return "admin/dashboard";
}

    @GetMapping("/services")
    public String services(Model model) {
        List<Service> services = serviceRepository.findAll();
        model.addAttribute("pageTitle", "Gestion des Services");
        model.addAttribute("pageDescription", "Administration des services de nettoyage");
        model.addAttribute("services", services);
        return "admin/services";
    }

    @GetMapping("/services/new")
    public String newService(Model model) {
        model.addAttribute("service", new Service());
        return "admin/service-form";
    }

    @PostMapping("/services")
    public String saveService(@ModelAttribute Service service, 
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             RedirectAttributes redirectAttributes) {
        try {
            service.setActive(true);
            service.setCreatedAt(LocalDateTime.now());
            
            // Gérer l'upload de l'image
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String imageFilename = fileStorageService.storeServiceImage(imageFile);
                    service.setImageUrl(imageFilename);
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("error", "Erreur lors de l'upload de l'image : " + e.getMessage());
                    return "redirect:/admin/services/new";
                } catch (IllegalArgumentException e) {
                    redirectAttributes.addFlashAttribute("error", e.getMessage());
                    return "redirect:/admin/services/new";
                }
            }
            
            serviceRepository.save(service);
            redirectAttributes.addFlashAttribute("success", "Service créé avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création du service : " + e.getMessage());
        }
        return "redirect:/admin/services";
    }

    @GetMapping("/services/{id}/edit")
    public String editService(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Service service = serviceRepository.findById(id).orElse(null);
            if (service == null) {
                redirectAttributes.addFlashAttribute("error", "Service non trouvé");
                return "redirect:/admin/services";
            }
            model.addAttribute("service", service);
            return "admin/service-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du chargement du service");
            return "redirect:/admin/services";
        }
    }

    @PostMapping("/services/{id}/update")
    public String updateService(@PathVariable Long id, 
                               @ModelAttribute Service service,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               RedirectAttributes redirectAttributes) {
        try {
            Service existingService = serviceRepository.findById(id).orElse(null);
            if (existingService == null) {
                redirectAttributes.addFlashAttribute("error", "Service non trouvé");
                return "redirect:/admin/services";
            }
            
            existingService.setName(service.getName());
            existingService.setDescription(service.getDescription());
            existingService.setPrice(service.getPrice());
            existingService.setActive(service.isActive());
            
            // Gérer l'upload de l'image seulement si un nouveau fichier est fourni
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String imageFilename = fileStorageService.storeServiceImage(imageFile);
                    existingService.setImageUrl(imageFilename);
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("error", "Erreur lors de l'upload de l'image : " + e.getMessage());
                    return "redirect:/admin/services/" + id + "/edit";
                } catch (IllegalArgumentException e) {
                    redirectAttributes.addFlashAttribute("error", e.getMessage());
                    return "redirect:/admin/services/" + id + "/edit";
                }
            }
            // Si aucune nouvelle image n'est fournie, on garde l'image existante
            
            serviceRepository.save(existingService);
            redirectAttributes.addFlashAttribute("success", "Service mis à jour avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour du service : " + e.getMessage());
        }
        return "redirect:/admin/services";
    }

    @PostMapping("/services/{id}/delete")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Service service = serviceRepository.findById(id).orElse(null);
            if (service == null) {
                redirectAttributes.addFlashAttribute("error", "Service non trouvé");
                return "redirect:/admin/services";
            }
            
            serviceRepository.delete(service);
            redirectAttributes.addFlashAttribute("success", "Service supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression du service");
        }
        return "redirect:/admin/services";
    }

    @GetMapping("/requests")
    public String requests(Model model) {
        List<ServiceRequest> requests = serviceRequestRepository.findAll();
        model.addAttribute("pageTitle", "Gestion des Demandes");
        model.addAttribute("pageDescription", "Gestion des demandes de service des clients");
        model.addAttribute("requests", requests);
        model.addAttribute("pendingRequests", serviceRequestRepository.countByStatus(ServiceRequest.Status.PENDING));
        model.addAttribute("confirmedRequests", serviceRequestRepository.countByStatus(ServiceRequest.Status.CONFIRMED));
        model.addAttribute("inProgressRequests", serviceRequestRepository.countByStatus(ServiceRequest.Status.IN_PROGRESS));
        model.addAttribute("completedRequests", serviceRequestRepository.countByStatus(ServiceRequest.Status.COMPLETED));
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

    @GetMapping("/reviews")
    public String reviews(Model model) {
        List<Review> reviews = reviewRepository.findAll();
        model.addAttribute("pageTitle", "Gestion des Avis");
        model.addAttribute("pageDescription", "Modération des avis des clients");
        model.addAttribute("reviews", reviews);
        model.addAttribute("pendingReviews", reviewRepository.findByApprovedFalseOrderByCreatedAtDesc().size());
        model.addAttribute("approvedReviews", reviewRepository.findByApprovedTrueOrderByCreatedAtDesc().size());
        model.addAttribute("totalReviews", reviews.size());
        
        // Calculate average rating
        double averageRating = reviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));
        
        return "admin/reviews";
    }

    @PostMapping("/reviews/{id}/approve")
    public String approveReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Review review = reviewRepository.findById(id).orElse(null);
            if (review != null) {
                review.setApproved(true);
                reviewRepository.save(review);
                redirectAttributes.addFlashAttribute("success", "Avis approuvé avec succès");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'approbation");
        }
        return "redirect:/admin/reviews";
    }

    @PostMapping("/reviews/{id}/reject")
    public String rejectReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Review review = reviewRepository.findById(id).orElse(null);
            if (review != null) {
                review.setApproved(false);
                reviewRepository.save(review);
                redirectAttributes.addFlashAttribute("success", "Avis rejeté");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du rejet");
        }
        return "redirect:/admin/reviews";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Review review = reviewRepository.findById(id).orElse(null);
            if (review == null) {
                redirectAttributes.addFlashAttribute("error", "Avis non trouvé");
                return "redirect:/admin/reviews";
            }
            
            reviewRepository.delete(review);
            redirectAttributes.addFlashAttribute("success", "Avis supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression de l'avis");
        }
        return "redirect:/admin/reviews";
    }

    @GetMapping("/messages")
    public String messages(Model model) {
        List<ContactMessage> messages = contactMessageRepository.findAll();
        model.addAttribute("pageTitle", "Gestion des Messages");
        model.addAttribute("pageDescription", "Gestion des messages de contact des clients");
        model.addAttribute("messages", messages);
        model.addAttribute("unreadMessages", contactMessageRepository.countUnreadMessages());
        return "admin/messages";
    }

    @PostMapping("/messages/{id}/mark-read")
    public String markMessageAsRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ContactMessage message = contactMessageRepository.findById(id).orElse(null);
            if (message != null) {
                message.setRead(true);
                if (message.getStatus() == ContactMessage.Status.NEW) {
                    message.setStatus(ContactMessage.Status.READ);
                }
                contactMessageRepository.save(message);
                redirectAttributes.addFlashAttribute("success", "Message marqué comme lu");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour du message");
        }
        return "redirect:/admin/messages";
    }

    @PostMapping("/messages/{id}/mark-read-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markMessageAsReadAjax(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            ContactMessage message = contactMessageRepository.findById(id).orElse(null);
            if (message != null && !message.isRead()) {
                message.setRead(true);
                if (message.getStatus() == ContactMessage.Status.NEW) {
                    message.setStatus(ContactMessage.Status.READ);
                }
                contactMessageRepository.save(message);
                response.put("success", true);
                response.put("message", "Message marqué comme lu");
            } else if (message != null && message.isRead()) {
                response.put("success", true);
                response.put("message", "Message déjà lu");
            } else {
                response.put("success", false);
                response.put("message", "Message non trouvé");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour du message");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/messages/{id}/delete")
    public String deleteMessage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ContactMessage message = contactMessageRepository.findById(id).orElse(null);
            if (message != null) {
                contactMessageRepository.delete(message);
                redirectAttributes.addFlashAttribute("success", "Message supprimé avec succès");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression du message");
        }
        return "redirect:/admin/messages";
    }

    @GetMapping("/users")
    public String users(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("pageTitle", "Gestion des Utilisateurs");
        model.addAttribute("pageDescription", "Gestion des utilisateurs de la plateforme");
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("pageTitle", "Créer un Utilisateur");
        model.addAttribute("pageDescription", "Créer un nouvel utilisateur");
        return "admin/user-form";
    }

    @PostMapping("/users")
    public String saveUser(User user, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        boolean isNewUser = user.getId() == null;
        
        // Valider manuellement les champs nécessaires
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            bindingResult.rejectValue("firstName", "NotBlank", "Le prénom est requis");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            bindingResult.rejectValue("lastName", "NotBlank", "Le nom est requis");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            bindingResult.rejectValue("email", "NotBlank", "L'email est requis");
        } else if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            bindingResult.rejectValue("email", "Email", "Email invalide");
        }
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            bindingResult.rejectValue("phone", "NotBlank", "Le téléphone est requis");
        }
        
        // Valider le mot de passe pour les nouveaux utilisateurs
        if (isNewUser) {
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                bindingResult.rejectValue("password", "NotBlank", "Le mot de passe est requis pour créer un nouvel utilisateur");
            }
        }
        
        // Vérifier si l'email existe déjà
        if (isNewUser && userRepository.existsByEmail(user.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Cet email est déjà utilisé");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("pageTitle", "Créer un Utilisateur");
            model.addAttribute("pageDescription", "Créer un nouvel utilisateur");
            return "admin/user-form";
        }

        try {
            if (isNewUser) {
                // Encoder le mot de passe fourni par l'admin
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setFirstLogin(true);
            } else {
                // Pour la modification, encoder le mot de passe seulement s'il est fourni
                if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                }
            }
            
            if (user.getRole() == null) {
                user.setRole(User.Role.CLIENT);
            }
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Utilisateur créé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création de l'utilisateur: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Utilisateur non trouvé");
                return "redirect:/admin/users";
            }
            model.addAttribute("user", user);
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("pageTitle", "Modifier l'utilisateur");
            model.addAttribute("pageDescription", "Modifier les informations de l'utilisateur");
            return "admin/user-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du chargement de l'utilisateur");
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id, User user, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        try {
            User existing = userRepository.findById(id).orElse(null);
            if (existing == null) {
                redirectAttributes.addFlashAttribute("error", "Utilisateur non trouvé");
                return "redirect:/admin/users";
            }

            // Check email uniqueness (allow same email for current user)
            userRepository.findByEmail(user.getEmail()).ifPresent(u -> {
                if (!u.getId().equals(id)) {
                    bindingResult.rejectValue("email", "error.user", "Cet email est déjà utilisé par un autre utilisateur");
                }
            });

            // Basic manual validation for update (password optional)
            if (user.getFirstName() == null || user.getFirstName().isBlank()) {
                bindingResult.rejectValue("firstName", "error.user", "Le prénom est requis");
            }
            if (user.getLastName() == null || user.getLastName().isBlank()) {
                bindingResult.rejectValue("lastName", "error.user", "Le nom est requis");
            }
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                bindingResult.rejectValue("email", "error.user", "L'email est requis");
            }
            if (user.getPhone() == null || user.getPhone().isBlank()) {
                bindingResult.rejectValue("phone", "error.user", "Le téléphone est requis");
            }

            if (bindingResult.hasErrors()) {
                // return to form with errors
                user.setId(id);
                model.addAttribute("user", user);
                model.addAttribute("roles", User.Role.values());
                model.addAttribute("pageTitle", "Modifier l'utilisateur");
                model.addAttribute("pageDescription", "Modifier les informations de l'utilisateur");
                return "admin/user-form";
            }

            existing.setFirstName(user.getFirstName());
            existing.setLastName(user.getLastName());
            existing.setEmail(user.getEmail());
            existing.setPhone(user.getPhone());
            existing.setRole(user.getRole());
            existing.setEnabled(user.isEnabled());

            // If a new password is provided, encode and set it
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            userRepository.save(existing);
            redirectAttributes.addFlashAttribute("success", "Utilisateur mis à jour avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour de l'utilisateur");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        List<Notification> notifications = notificationRepository.findAll();
        model.addAttribute("pageTitle", "Centre de Notifications");
        model.addAttribute("pageDescription", "Gestion des notifications du système");
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotifications", notificationRepository.countUnreadNotifications());
        return "admin/notifications";
    }

    @PostMapping("/notifications/{id}/mark-read")
    public String markNotificationAsRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Notification notification = notificationRepository.findById(id).orElse(null);
            if (notification != null) {
                notification.setRead(true);
                notificationRepository.save(notification);
                redirectAttributes.addFlashAttribute("success", "Notification marquée comme lue");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour de la notification");
        }
        return "redirect:/admin/notifications";
    }

    @PostMapping("/notifications/{id}/delete")
    public String deleteNotification(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Notification notification = notificationRepository.findById(id).orElse(null);
            if (notification != null) {
                notificationRepository.delete(notification);
                redirectAttributes.addFlashAttribute("success", "Notification supprimée avec succès");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression de la notification");
        }
        return "redirect:/admin/notifications";
    }


    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                user.setEnabled(!user.isEnabled());
                userRepository.save(user);
                redirectAttributes.addFlashAttribute("success", "Statut de l'utilisateur mis à jour");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour de l'utilisateur");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                if (user.getRole() == User.Role.ADMIN) {
                    redirectAttributes.addFlashAttribute("error", "Impossible de supprimer un administrateur");
                } else {
                    // Supprimer toutes les données liées avant de supprimer l'utilisateur
                    // 1. Supprimer les absences
                    List<Absence> absences = absenceRepository.findByUserOrderByAbsenceDateDesc(user);
                    if (absences != null && !absences.isEmpty()) {
                        absenceRepository.deleteAll(absences);
                    }
                    
                    // 2. Supprimer les suivis de temps
                    List<TimeTracking> timeTrackings = timeTrackingRepository.findByUserOrderByTrackingDateDesc(user);
                    if (timeTrackings != null && !timeTrackings.isEmpty()) {
                        timeTrackingRepository.deleteAll(timeTrackings);
                    }
                    
                    // 3. Supprimer les observations de lieu de travail
                    List<WorkplaceObservation> observations = workplaceObservationRepository.findByUserOrderByCreatedAtDesc(user);
                    if (observations != null && !observations.isEmpty()) {
                        workplaceObservationRepository.deleteAll(observations);
                    }
                    
                    // 4. Supprimer les avis (reviews peuvent avoir user null, donc on cherche par user)
                    List<Review> allReviews = reviewRepository.findAll();
                    List<Review> userReviews = allReviews.stream()
                        .filter(r -> r.getUser() != null && r.getUser().getId().equals(id))
                        .collect(Collectors.toList());
                    if (!userReviews.isEmpty()) {
                        reviewRepository.deleteAll(userReviews);
                    }
                    
                    // 5. Supprimer les demandes de service
                    List<ServiceRequest> serviceRequests = serviceRequestRepository.findByUser(user);
                    if (serviceRequests != null && !serviceRequests.isEmpty()) {
                        serviceRequestRepository.deleteAll(serviceRequests);
                    }
                    
                    // 6. Supprimer l'utilisateur
                    userRepository.delete(user);
                    redirectAttributes.addFlashAttribute("success", "Utilisateur supprimé avec succès");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Utilisateur non trouvé");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ========== GESTION DES EMPLOYÉS ==========
    @GetMapping("/employees")
    public String employees(Model model) {
        List<User> employees = userRepository.findAll().stream()
            .filter(u -> u.getRole() == User.Role.EMPLOYEE)
            .toList();
        model.addAttribute("employees", employees);
        model.addAttribute("pageTitle", "Gestion des Employés");
        return "admin/employees";
    }

    @GetMapping("/employees/new")
    public String newEmployee(Model model) {
        model.addAttribute("user", new User());
        return "admin/employee-form";
    }

    /**
     * Génère un mot de passe aléatoire sécurisé
     * Utilise uniquement des majuscules (A-Z) et des chiffres (0-9)
     * @param length Longueur du mot de passe (par défaut 10)
     * @return Mot de passe aléatoire
     */
    private String generateRandomPassword(int length) {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        String allChars = upperCase + numbers;
        
        java.util.Random random = new java.util.Random();
        StringBuilder password = new StringBuilder();
        
        // Assurer au moins une majuscule et un chiffre
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        
        // Remplir le reste avec des caractères aléatoires (A-Z et 0-9 uniquement)
        for (int i = password.length(); i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Mélanger les caractères pour éviter un pattern prévisible
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    @PostMapping("/employees")
    public String saveEmployee(User user, @RequestParam(required = false) String password, 
                              BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        boolean isNewEmployee = user.getId() == null;
        
        // Valider manuellement les champs nécessaires
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            bindingResult.rejectValue("firstName", "NotBlank", "Le prénom est requis");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            bindingResult.rejectValue("lastName", "NotBlank", "Le nom est requis");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            bindingResult.rejectValue("email", "NotBlank", "L'email est requis");
        } else if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            bindingResult.rejectValue("email", "Email", "Email invalide");
        }
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            bindingResult.rejectValue("phone", "NotBlank", "Le téléphone est requis");
        }
        
        // Valider le mot de passe pour les nouveaux employés
        if (isNewEmployee) {
            if (password == null || password.trim().isEmpty()) {
                bindingResult.rejectValue("password", "NotBlank", "Le mot de passe est requis");
            }
        }
        
        // Vérifier les erreurs de validation
        if (bindingResult.hasErrors()) {
            if (isNewEmployee) {
                user.setPassword(null); // Réinitialiser pour éviter l'affichage
            }
            model.addAttribute("user", user);
            return "admin/employee-form";
        }

        try {
            if (isNewEmployee && userRepository.existsByEmail(user.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Cet email est déjà utilisé");
                return "redirect:/admin/employees/new";
            }

            user.setRole(User.Role.EMPLOYEE);
            // Gérer le mot de passe pour les nouveaux employés
            if (isNewEmployee) {
                // Utiliser le mot de passe fourni par l'admin (obligatoire)
                user.setPassword(passwordEncoder.encode(password));
                user.setFirstLogin(true);
            }
            user.setEnabled(true);
            userRepository.save(user);

            // Utiliser isNewEmployee au lieu de user.getId() == null car après save(), l'ID n'est plus null
            if (isNewEmployee) {
                redirectAttributes.addFlashAttribute("success", 
                    "Employé créé avec succès. Identifiants - Email: " + user.getEmail());
            } else {
                redirectAttributes.addFlashAttribute("success", "Employé modifié avec succès");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création de l'employé: " + e.getMessage());
        }

        return "redirect:/admin/employees";
    }

    @GetMapping("/employees/{id}/edit")
    public String editEmployee(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user != null && user.getRole() == User.Role.EMPLOYEE) {
                model.addAttribute("user", user);
                return "admin/employee-form";
            } else {
                redirectAttributes.addFlashAttribute("error", "Employé non trouvé");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la récupération de l'employé");
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/employees/{id}/update")
    public String updateEmployee(@PathVariable Long id, User user, RedirectAttributes redirectAttributes) {
        try {
            User existing = userRepository.findById(id).orElse(null);
            if (existing != null) {
                existing.setFirstName(user.getFirstName());
                existing.setLastName(user.getLastName());
                existing.setPhone(user.getPhone());
                existing.setEnabled(user.isEnabled());
                userRepository.save(existing);
                redirectAttributes.addFlashAttribute("success", "Employé mis à jour avec succès");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour");
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Employé non trouvé");
                return "redirect:/admin/employees";
            }
            if (user.getRole() == User.Role.ADMIN) {
                redirectAttributes.addFlashAttribute("error", "Impossible de supprimer un administrateur");
                return "redirect:/admin/employees";
            }
            userRepository.delete(user);
            redirectAttributes.addFlashAttribute("success", "Employé supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression de l'employé");
        }
        return "redirect:/admin/employees";
    }

    @GetMapping("/employees/{id}/profile")
    public String employeeProfile(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User employee = userRepository.findById(id).orElse(null);
            if (employee != null && employee.getRole() == User.Role.EMPLOYEE) {
                List<TimeTracking> timeTrackings = timeTrackingRepository.findByUserOrderByTrackingDateDesc(employee);
                List<Absence> absences = absenceRepository.findByUserOrderByAbsenceDateDesc(employee);
                List<WorkplaceObservation> observations = workplaceObservationRepository.findByUserOrderByCreatedAtDesc(employee);

                model.addAttribute("employee", employee);
                model.addAttribute("timeTrackings", timeTrackings);
                model.addAttribute("absences", absences);
                model.addAttribute("observations", observations);
                model.addAttribute("pageTitle", "Profil de " + employee.getFullName());
                return "admin/employee-profile";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la récupération du profil");
        }
        return "redirect:/admin/employees";
    }

    @GetMapping("/employees/{id}/detail")
    public String employeeDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User employee = userRepository.findById(id).orElse(null);
            if (employee != null && employee.getRole() == User.Role.EMPLOYEE) {
                // Get today's tracking
                java.time.LocalDate today = java.time.LocalDate.now();
                TimeTracking todayTracking = timeTrackingRepository.findByUserAndTrackingDate(employee, today).orElse(null);
                
                // Get this month's absences
                java.time.YearMonth currentMonth = java.time.YearMonth.now();
                List<Absence> monthlyAbsences = absenceRepository.findByUserOrderByAbsenceDateDesc(employee).stream()
                    .filter(a -> java.time.YearMonth.from(a.getAbsenceDate()).equals(currentMonth))
                    .toList();
                
                // Get observations
                List<WorkplaceObservation> observations = workplaceObservationRepository.findByUserOrderByCreatedAtDesc(employee).stream()
                    .limit(10)
                    .toList();

                model.addAttribute("employee", employee);
                model.addAttribute("todayTracking", todayTracking);
                model.addAttribute("monthlyAbsences", monthlyAbsences);
                model.addAttribute("observations", observations);
                model.addAttribute("pageTitle", "Détail - " + employee.getFullName());
                return "admin/employee-detail";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la récupération du détail de l'employé");
        }
        return "redirect:/admin/employees";
    }

    @GetMapping("/employees/{id}/time-tracking")
    public String employeeTimeTracking(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User employee = userRepository.findById(id).orElse(null);
            if (employee != null && employee.getRole() == User.Role.EMPLOYEE) {
                List<TimeTracking> timeTrackings = timeTrackingRepository.findByUserOrderByTrackingDateDesc(employee);
                model.addAttribute("employee", employee);
                model.addAttribute("timeTrackings", timeTrackings);
                model.addAttribute("pageTitle", "Pointages - " + employee.getFullName());
                return "admin/employee-time-tracking";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la récupération des pointages");
        }
        return "redirect:/admin/employees";
    }

    @GetMapping("/employees/{id}/absences")
    public String employeeAbsences(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User employee = userRepository.findById(id).orElse(null);
            if (employee != null && employee.getRole() == User.Role.EMPLOYEE) {
                List<Absence> absences = absenceRepository.findByUserOrderByAbsenceDateDesc(employee);
                model.addAttribute("employee", employee);
                model.addAttribute("absences", absences);
                model.addAttribute("pageTitle", "Absences - " + employee.getFullName());
                return "admin/employee-absences";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la récupération des absences");
        }
        return "redirect:/admin/employees";
    }

    @GetMapping("/employees/{id}/observations")
    public String employeeObservations(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User employee = userRepository.findById(id).orElse(null);
            if (employee != null && employee.getRole() == User.Role.EMPLOYEE) {
                List<WorkplaceObservation> observations = workplaceObservationRepository.findByUserOrderByCreatedAtDesc(employee);
                if (observations == null) {
                    observations = new java.util.ArrayList<>();
                }
                System.out.println("AdminController: Récupération des observations pour l'employé: " + employee.getEmail());
                System.out.println("AdminController: Nombre d'observations trouvées: " + observations.size());
                for (WorkplaceObservation obs : observations) {
                    System.out.println("AdminController: Observation ID=" + obs.getId() + 
                        ", Title=" + obs.getTitle() + 
                        ", Priority=" + (obs.getPriority() != null ? obs.getPriority().name() : "null") +
                        ", Status=" + (obs.getStatus() != null ? obs.getStatus().name() : "null") +
                        ", PhotoPath=" + obs.getPhotoPath() +
                        ", PhotoPaths count=" + obs.getPhotoPaths().size());
                }
                model.addAttribute("employee", employee);
                model.addAttribute("observations", observations);
                model.addAttribute("pageTitle", "Observations - " + employee.getFullName());
                return "admin/employee-observations";
            } else {
                System.out.println("AdminController: Employé non trouvé ou n'est pas un employé. ID=" + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("AdminController: Erreur lors de la récupération des observations: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la récupération des observations: " + e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/observations/{id}/status")
    public String updateObservationStatus(@PathVariable Long id, @RequestParam WorkplaceObservation.Status status,
                                         @RequestParam(required = false) String adminNotes,
                                         RedirectAttributes redirectAttributes) {
        try {
            WorkplaceObservation observation = workplaceObservationRepository.findById(id).orElse(null);
            if (observation != null) {
                observation.setStatus(status);
                if (adminNotes != null && !adminNotes.isEmpty()) {
                    observation.setAdminNotes(adminNotes);
                }
                if (status == WorkplaceObservation.Status.RESOLVED || status == WorkplaceObservation.Status.CLOSED) {
                    observation.setResolvedAt(LocalDateTime.now());
                }
                workplaceObservationRepository.save(observation);
                redirectAttributes.addFlashAttribute("success", "Statut mise à jour avec succès");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour du statut");
        }
        return "redirect:/admin/employees";
    }
    
    // ========== BACKUP MANAGEMENT ==========
    @GetMapping("/backup")
    public String backupPage(Model model) {
        List<String> backups = backupService.listBackups();
        model.addAttribute("backups", backups);
        model.addAttribute("pageTitle", "Gestion des Backups");
        model.addAttribute("pageDescription", "Gérer les backups de la base de données");
        return "admin/backup";
    }
    
    @PostMapping("/backup/create")
    public String createBackup(RedirectAttributes redirectAttributes) {
        boolean success = backupService.performBackup();
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Backup créé avec succès");
        } else {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création du backup");
        }
        return "redirect:/admin/backup";
    }
    
    @GetMapping("/backup/download")
    public ResponseEntity<Resource> downloadBackup(@RequestParam String filename) {
        try {
            // Sécuriser le nom du fichier pour éviter les path traversal
            String safeFilename = filename.replaceAll("[^a-zA-Z0-9._-]", "");
            if (!safeFilename.endsWith(".sql")) {
                return ResponseEntity.badRequest().build();
            }
            
            // Construire le chemin du fichier
            Path backupPath = Paths.get("./backups").resolve(safeFilename);
            
            if (!Files.exists(backupPath) || !Files.isRegularFile(backupPath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(backupPath);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + safeFilename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
