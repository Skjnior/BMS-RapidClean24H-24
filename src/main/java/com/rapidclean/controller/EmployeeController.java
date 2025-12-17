package com.rapidclean.controller;

import com.rapidclean.entity.*;
import com.rapidclean.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TimeTrackingRepository timeTrackingRepository;

    @Autowired
    private AbsenceRepository absenceRepository;

    @Autowired
    private WorkplaceObservationRepository workplaceObservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.rapidclean.service.FileStorageService fileStorageService;

    // ========== DASHBOARD ==========
    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        
        if (user != null) {
            // Vérifier si c'est la première connexion
            if (user.isFirstLogin()) {
                return "redirect:/employee/change-password";
            }
            
            LocalDate today = LocalDate.now();
            LocalDate monthStart = today.withDayOfMonth(1);
            LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
            
            // Récupérer les données du mois en cours
            List<TimeTracking> monthlyTimeTracking = timeTrackingRepository
                .findByUserAndTrackingDateBetweenOrderByTrackingDateDesc(user, monthStart, today);
            
            // Récupérer toutes les absences du mois (du 1er au dernier jour du mois)
            List<Absence> monthlyAbsences = absenceRepository
                .findByUserAndAbsenceDateBetweenOrderByAbsenceDateDesc(user, monthStart, monthEnd);
            
            // Si la liste est null, initialiser une liste vide
            if (monthlyAbsences == null) {
                monthlyAbsences = new ArrayList<>();
            }
            
            List<WorkplaceObservation> observations = workplaceObservationRepository
                .findByUserOrderByCreatedAtDesc(user);
            
            // Pointage du jour
            TimeTracking todayTracking = timeTrackingRepository
                .findByUserAndTrackingDate(user, today)
                .orElse(null);
            
            model.addAttribute("user", user);
            model.addAttribute("todayTracking", todayTracking);
            model.addAttribute("monthlyTimeTracking", monthlyTimeTracking);
            model.addAttribute("monthlyAbsences", monthlyAbsences);
            model.addAttribute("observations", observations);
            model.addAttribute("pageTitle", "Tableau de Bord Employé");
            model.addAttribute("monthStart", monthStart);
            model.addAttribute("today", today);
        }
        
        return "employee/dashboard";
    }

    // ========== CHANGEMENT DE MOT DE PASSE OBLIGATOIRE ==========
    @GetMapping("/change-password")
    public String changePasswordForm(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Modifier votre mot de passe");
        }
        return "employee/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Principal principal,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        
        if (user != null) {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas");
                return "redirect:/employee/change-password";
            }
            
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Le mot de passe doit contenir au moins 6 caractères");
                return "redirect:/employee/change-password";
            }
            
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setFirstLogin(false);
            userRepository.save(user);
            
            // Déconnexion automatique et redirection vers la page d'accueil
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, null);
            
            // Redirection vers la page d'accueil avec message de succès dans l'URL
            // (on utilise un paramètre car la session est invalidée après logout)
            return "redirect:/?passwordChanged=true";
        }
        
        return "redirect:/employee/change-password";
    }

    // ========== POINTAGE DES HEURES ==========
    @GetMapping("/time-tracking")
    public String timeTrackingPage(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        
        if (user != null) {
            List<TimeTracking> trackings = timeTrackingRepository.findByUserOrderByTrackingDateDesc(user);
            TimeTracking todayTracking = timeTrackingRepository
                .findByUserAndTrackingDate(user, LocalDate.now())
                .orElse(null);
            
            model.addAttribute("user", user);
            model.addAttribute("trackings", trackings);
            model.addAttribute("todayTracking", todayTracking);
            model.addAttribute("pageTitle", "Pointage des Heures");
        }
        
        return "employee/time-tracking";
    }

    @PostMapping("/time-tracking/arrival")
    public String markArrival(Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        
        if (user != null) {
            LocalDate today = LocalDate.now();
            TimeTracking tracking = timeTrackingRepository
                .findByUserAndTrackingDate(user, today)
                .orElse(new TimeTracking(user, today));
            
            tracking.setArrivalTime(LocalTime.now());
            timeTrackingRepository.save(tracking);
            
            redirectAttributes.addFlashAttribute("success", "Heure d'arrivée enregistrée à " + tracking.getArrivalTime());
        }
        
        return "redirect:/employee/time-tracking";
    }

    @PostMapping("/time-tracking/departure")
    public String markDeparture(Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        
        if (user != null) {
            LocalDate today = LocalDate.now();
            TimeTracking tracking = timeTrackingRepository
                .findByUserAndTrackingDate(user, today)
                .orElse(new TimeTracking(user, today));
            
            if (tracking.getArrivalTime() == null) {
                redirectAttributes.addFlashAttribute("error", "Veuillez d'abord enregistrer votre heure d'arrivée");
                return "redirect:/employee/time-tracking";
            }
            
            tracking.setDepartureTime(LocalTime.now());
            timeTrackingRepository.save(tracking);
            
            redirectAttributes.addFlashAttribute("success", "Heure de départ enregistrée à " + tracking.getDepartureTime());
        }
        
        return "redirect:/employee/time-tracking";
    }

    // ========== DÉCLARATION D'ABSENCE ==========
    @GetMapping("/absences")
    public String absencesPage(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        
        if (user != null) {
            List<Absence> absences = absenceRepository.findByUserOrderByAbsenceDateDesc(user);
            model.addAttribute("user", user);
            model.addAttribute("absences", absences);
            model.addAttribute("absenceTypes", Absence.AbsenceType.values());
            model.addAttribute("pageTitle", "Déclaration d'Absence");
        }
        
        return "employee/absences";
    }

    @PostMapping("/absences")
    public String declareAbsence(@RequestParam LocalDate absenceDate,
                                @RequestParam Absence.AbsenceType type,
                                @RequestParam String reason,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        
        if (user != null) {
            Absence absence = new Absence(user, absenceDate, type);
            absence.setReason(reason);
            absenceRepository.save(absence);
            
            redirectAttributes.addFlashAttribute("success", "Absence déclarée avec succès");
        }
        
        return "redirect:/employee/absences";
    }

    // ========== OBSERVATIONS AU LIEU DE TRAVAIL ==========
    @GetMapping("/observations")
    public String observationsPage(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        
        List<WorkplaceObservation> observations = new java.util.ArrayList<>();
        if (user != null) {
            observations = workplaceObservationRepository
                .findByUserOrderByCreatedAtDesc(user);
            System.out.println("EmployeeController: Récupération des observations pour l'utilisateur: " + user.getEmail());
            System.out.println("EmployeeController: Nombre d'observations trouvées: " + observations.size());
            for (WorkplaceObservation obs : observations) {
                System.out.println("EmployeeController: Observation ID=" + obs.getId() + 
                    ", Title=" + obs.getTitle() + 
                    ", Priority=" + (obs.getPriority() != null ? obs.getPriority().name() : "null") +
                    ", Status=" + (obs.getStatus() != null ? obs.getStatus().name() : "null") +
                    ", PhotoPath=" + obs.getPhotoPath() +
                    ", PhotoPaths count=" + obs.getPhotoPaths().size());
            }
        } else {
            System.out.println("EmployeeController: Utilisateur non trouvé pour: " + principal.getName());
        }
        
        model.addAttribute("user", user);
        model.addAttribute("observations", observations);
        model.addAttribute("priorities", WorkplaceObservation.Priority.values());
        model.addAttribute("pageTitle", "Observations au Lieu de Travail");
        
        return "employee/observations";
    }

    @PostMapping("/observations")
    public String reportObservation(@RequestParam String title,
                                   @RequestParam String description,
                                   @RequestParam(required = false) WorkplaceObservation.Priority priority,
                                   @RequestParam(required = false) org.springframework.web.multipart.MultipartFile[] photos,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        
        if (user != null) {
            WorkplaceObservation observation = new WorkplaceObservation(user, title, description);
            
            if (priority != null) {
                observation.setPriority(priority);
            } else {
                observation.setPriority(WorkplaceObservation.Priority.MEDIUM);
            }
            
            // Gérer l'upload des images multiples
            if (photos != null && photos.length > 0) {
                try {
                    List<String> photoPaths = fileStorageService.storeFiles(photos);
                    if (!photoPaths.isEmpty()) {
                        observation.setPhotoPaths(photoPaths);
                    }
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", 
                        "Erreur lors de l'upload des images: " + e.getMessage());
                    return "redirect:/employee/observations";
                }
            }
            
            workplaceObservationRepository.save(observation);
            
            redirectAttributes.addFlashAttribute("success", 
                "Observation signalée avec succès. Un administrateur sera notifié.");
        }
        
        return "redirect:/employee/observations";
    }

    // ========== PROFIL DE L'EMPLOYÉ ==========
    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Mon Profil");
        }
        
        return "employee/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String phone,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        
        if (user != null) {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès");
        }
        
        return "redirect:/employee/profile";
    }
}
