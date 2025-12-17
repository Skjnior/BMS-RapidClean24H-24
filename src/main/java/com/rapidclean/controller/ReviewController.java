package com.rapidclean.controller;

import com.rapidclean.entity.Review;
import com.rapidclean.entity.Notification;
import com.rapidclean.repository.ReviewRepository;
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
import java.util.List;

@Controller
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private SpamProtectionService spamProtectionService;

    @GetMapping("/submit-review")
    public String reviewForm(Model model) {
        model.addAttribute("review", new Review());
        return "review-form";
    }

    @PostMapping("/submit-review")
    public String submitReview(
            @ModelAttribute Review review,
            @RequestParam(value = "website", required = false) String honeypot,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            // Vérification honeypot
            if (!spamProtectionService.isHoneypotValid(honeypot)) {
                redirectAttributes.addFlashAttribute("error", "Requête invalide.");
                return "redirect:/submit-review";
            }
            
            // Vérification rate limiting
            String clientIp = getClientIp(request);
            String identifier = clientIp + "_review_" + (review.getCustomerName() != null ? review.getCustomerName() : "");
            
            if (!rateLimitingService.isAllowed(identifier)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Trop de requêtes. Veuillez patienter quelques minutes avant de réessayer.");
                return "redirect:/submit-review";
            }
            
            // Vérification spam
            String fullContent = (review.getComment() != null ? review.getComment() : "");
            if (spamProtectionService.isSpam(fullContent, review.getCustomerEmail())) {
                redirectAttributes.addFlashAttribute("error", 
                    "Votre avis n'a pas pu être envoyé. Veuillez vérifier le contenu.");
                return "redirect:/submit-review";
            }
            
            // Créer l'avis
            review.setApproved(false); // En attente d'approbation
            review.setCreatedAt(LocalDateTime.now());
            
            reviewRepository.save(review);
            
            // Réinitialiser le rate limiting après succès
            rateLimitingService.reset(identifier);

            // Créer une notification pour l'admin
            Notification notification = new Notification();
            notification.setTitle("Nouvel avis client");
            notification.setMessage("Un nouvel avis a été soumis par " + review.getCustomerName() + 
                                 " avec une note de " + review.getRating() + "/5 étoiles");
            notification.setType(Notification.Type.NEW_REVIEW);
            notification.setPriority(Notification.Priority.MEDIUM);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(notification);

            redirectAttributes.addFlashAttribute("success", 
                "✅ Avis envoyé avec succès ! Merci pour votre retour. Il sera publié après validation par notre équipe.");
            redirectAttributes.addFlashAttribute("redirectToHome", true);
            
            return "redirect:/submit-review";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Une erreur est survenue. Veuillez réessayer.");
            return "redirect:/submit-review";
        }
    }

    // Méthode pour récupérer les avis approuvés (pour l'affichage sur le site)
    public List<Review> getApprovedReviews() {
        return reviewRepository.findByApprovedTrueOrderByCreatedAtDesc();
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