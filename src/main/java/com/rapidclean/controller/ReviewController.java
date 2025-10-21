package com.rapidclean.controller;

import com.rapidclean.entity.Review;
import com.rapidclean.repository.ReviewRepository;
import com.rapidclean.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/reviews")
    public String reviews(Model model) {
        List<Review> approvedReviews = reviewRepository.findByApprovedTrueOrderByCreatedAtDesc();
        model.addAttribute("reviews", approvedReviews);
        return "reviews";
    }

    @PostMapping("/reviews")
    public String submitReview(Review review, RedirectAttributes redirectAttributes) {
        try {
            reviewRepository.save(review);
            
            // Créer une notification pour l'admin
            notificationService.createNotification(
                "Nouvel Avis Client",
                "Un nouveau avis a été soumis par " + review.getCustomerName(),
                com.rapidclean.entity.Notification.Type.NEW_REVIEW,
                com.rapidclean.entity.Notification.Priority.MEDIUM
            );
            
            redirectAttributes.addFlashAttribute("success", "Votre avis a été soumis avec succès. Il sera publié après validation.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la soumission de l'avis");
        }
        return "redirect:/reviews";
    }

    @GetMapping("/admin/reviews")
    public String adminReviews(Model model) {
        List<Review> pendingReviews = reviewRepository.findByApprovedFalseOrderByCreatedAtDesc();
        List<Review> approvedReviews = reviewRepository.findByApprovedTrueOrderByCreatedAtDesc();
        
        model.addAttribute("pendingReviews", pendingReviews);
        model.addAttribute("approvedReviews", approvedReviews);
        
        return "admin/reviews";
    }

    @PostMapping("/admin/reviews/{id}/approve")
    public String approveReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Review review = reviewRepository.findById(id).orElse(null);
            if (review != null) {
                review.setApproved(true);
                reviewRepository.save(review);
                redirectAttributes.addFlashAttribute("success", "Avis approuvé avec succès");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'approbation de l'avis");
        }
        return "redirect:/admin/reviews";
    }

    @PostMapping("/admin/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Avis supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression de l'avis");
        }
        return "redirect:/admin/reviews";
    }
}
