package com.rapidclean.controller;

import com.rapidclean.entity.ContactMessage;
import com.rapidclean.entity.Review;
import com.rapidclean.entity.Service;
import com.rapidclean.repository.ContactMessageRepository;
import com.rapidclean.repository.ReviewRepository;
import com.rapidclean.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/")
    public String home(Model model, @RequestParam(required = false) Boolean passwordChanged) {
        List<Service> services = serviceRepository.findByActiveTrue();
        List<Review> reviews = reviewRepository.findByApprovedTrueOrderByCreatedAtDesc();
        
        // Trier manuellement selon l'ordre spécifié
        services.sort((s1, s2) -> {
            String[] order = {
                "Nettoyage professionnel des fast-food",
                "Nettoyage des salles", 
                "Entretien sanitaires",
                "Nettoyage des vitres",
                "Nettoyage de cuisine",
                "Caisse et divers",
                "Locaux techniques et arrières",
                "Vestiaires et douches",
                "Nettoyage extérieurs",
                "Nettoyage des équipements de friture",
                "Nettoyage des systèmes de ventilation",
                "Nettoyage des sols industriels"
            };
            
            int index1 = java.util.Arrays.asList(order).indexOf(s1.getName());
            int index2 = java.util.Arrays.asList(order).indexOf(s2.getName());
            
            if (index1 == -1) index1 = 999;
            if (index2 == -1) index2 = 999;
            
            return Integer.compare(index1, index2);
        });
        
        model.addAttribute("services", services);
        model.addAttribute("reviews", reviews);
        model.addAttribute("contactMessage", new ContactMessage());
        model.addAttribute("review", new Review());
        
        // Gérer le message de succès après changement de mot de passe
        if (passwordChanged != null && passwordChanged) {
            model.addAttribute("success", "✅ Mot de passe modifié avec succès ! Veuillez vous reconnecter avec votre nouveau mot de passe.");
        }
        
        return "landing";
    }
    
    @GetMapping("/privacy-policy")
    public String privacyPolicy(Model model) {
        model.addAttribute("title", "Politique de Confidentialité - BMS RapidClean");
        return "privacy-policy";
    }
    
    @GetMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("title", "FAQ - Questions Fréquentes - BMS RapidClean");
        return "faq";
    }
    
    @GetMapping("/error")
    public String error(Model model) {
        return "error";
    }
}
