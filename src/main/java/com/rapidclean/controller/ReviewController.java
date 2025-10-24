package com.rapidclean.controller;

import com.rapidclean.entity.Review;
import com.rapidclean.entity.Notification;
import com.rapidclean.repository.ReviewRepository;
import com.rapidclean.repository.NotificationRepository;
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

    @GetMapping("/submit-review")
    public String reviewForm(Model model) {
        model.addAttribute("review", new Review());
        return "review-form";
    }

    @PostMapping("/submit-review")
    public String submitReview(@ModelAttribute Review review,
                             RedirectAttributes redirectAttributes) {
        try {
            // Créer l'avis
            review.setApproved(false); // En attente d'approbation
            review.setCreatedAt(LocalDateTime.now());
            
            Review savedReview = reviewRepository.save(review);

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
                "Merci pour votre avis ! Il sera publié après validation par notre équipe.");
            
            return "redirect:/";
            
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
}