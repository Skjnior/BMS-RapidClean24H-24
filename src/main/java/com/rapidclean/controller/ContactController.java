package com.rapidclean.controller;

import com.rapidclean.entity.ContactMessage;
import com.rapidclean.repository.ContactMessageRepository;
import com.rapidclean.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ContactController {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("contactMessage", new ContactMessage());
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(ContactMessage contactMessage, RedirectAttributes redirectAttributes) {
        try {
            contactMessageRepository.save(contactMessage);
            
            // Créer une notification pour l'admin
            notificationService.createNotification(
                "Nouveau Message de Contact",
                "Un nouveau message a été reçu de " + contactMessage.getFullName() + " - " + contactMessage.getSubject(),
                com.rapidclean.entity.Notification.Type.NEW_MESSAGE,
                com.rapidclean.entity.Notification.Priority.HIGH
            );
            
            redirectAttributes.addFlashAttribute("success", "Votre message a été envoyé avec succès. Nous vous répondrons dans les plus brefs délais.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'envoi du message");
        }
        return "redirect:/contact";
    }

    @GetMapping("/admin/messages")
    public String adminMessages(Model model) {
        List<ContactMessage> newMessages = contactMessageRepository.findByStatusOrderByCreatedAtDesc(ContactMessage.Status.NEW);
        List<ContactMessage> allMessages = contactMessageRepository.findAll();
        
        model.addAttribute("newMessages", newMessages);
        model.addAttribute("allMessages", allMessages);
        model.addAttribute("unreadCount", contactMessageRepository.countUnreadMessages());
        
        return "admin/messages";
    }

    @PostMapping("/admin/messages/{id}/read")
    public String markAsRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ContactMessage message = contactMessageRepository.findById(id).orElse(null);
            if (message != null) {
                message.setRead(true);
                message.setReadAt(LocalDateTime.now());
                message.setStatus(ContactMessage.Status.READ);
                contactMessageRepository.save(message);
                redirectAttributes.addFlashAttribute("success", "Message marqué comme lu");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour du message");
        }
        return "redirect:/admin/messages";
    }

    @PostMapping("/admin/messages/{id}/reply")
    public String markAsReplied(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ContactMessage message = contactMessageRepository.findById(id).orElse(null);
            if (message != null) {
                message.setStatus(ContactMessage.Status.REPLIED);
                contactMessageRepository.save(message);
                redirectAttributes.addFlashAttribute("success", "Message marqué comme répondu");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour du message");
        }
        return "redirect:/admin/messages";
    }
}
