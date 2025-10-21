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

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @GetMapping("/")
    public String home(Model model) {
        List<Service> services = serviceRepository.findByActiveTrue();
        List<Review> reviews = reviewRepository.findByApprovedTrueOrderByCreatedAtDesc();
        
        model.addAttribute("services", services);
        model.addAttribute("reviews", reviews);
        model.addAttribute("contactMessage", new ContactMessage());
        model.addAttribute("review", new Review());
        return "landing";
    }



}
