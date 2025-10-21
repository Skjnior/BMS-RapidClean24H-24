package com.rapidclean.controller;

import com.rapidclean.entity.Service;
import com.rapidclean.entity.ServiceRequest;
import com.rapidclean.entity.User;
import com.rapidclean.repository.ServiceRepository;
import com.rapidclean.repository.ServiceRequestRepository;
import com.rapidclean.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user != null) {
            List<ServiceRequest> requests = serviceRequestRepository.findByUser(user);
            model.addAttribute("requests", requests);
            model.addAttribute("user", user);
        }
        return "client/dashboard";
    }

    @GetMapping("/services")
    public String services(Model model) {
        List<Service> services = serviceRepository.findByActiveTrue();
        model.addAttribute("services", services);
        return "client/services";
    }

    @GetMapping("/request")
    public String requestForm(Model model) {
        List<Service> services = serviceRepository.findByActiveTrue();
        model.addAttribute("serviceRequest", new ServiceRequest());
        model.addAttribute("services", services);
        return "client/request-form";
    }

    @PostMapping("/request")
    public String submitRequest(ServiceRequest serviceRequest, Principal principal, 
                              RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                serviceRequest.setUser(user);
                serviceRequest.setServiceDate(LocalDateTime.now().plusDays(1)); // Default to tomorrow
                serviceRequestRepository.save(serviceRequest);
                redirectAttributes.addFlashAttribute("success", "Demande soumise avec succès");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la soumission de la demande");
        }
        return "redirect:/client/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        model.addAttribute("user", user);
        return "client/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(User updatedUser, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                user.setFirstName(updatedUser.getFirstName());
                user.setLastName(updatedUser.getLastName());
                user.setPhone(updatedUser.getPhone());
                userRepository.save(user);
                redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour du profil");
        }
        return "redirect:/client/profile";
    }
}
