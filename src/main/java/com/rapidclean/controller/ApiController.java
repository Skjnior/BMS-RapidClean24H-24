package com.rapidclean.controller;

import com.rapidclean.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/notifications/count")
    public Map<String, Long> getNotificationsCount() {
        long count = notificationRepository.countUnreadNotifications();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return response;
    }
}


