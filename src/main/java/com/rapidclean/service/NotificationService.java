package com.rapidclean.service;

import com.rapidclean.entity.Notification;
import com.rapidclean.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotification(String title, String message, Notification.Type type, Notification.Priority priority) {
        Notification notification = new Notification(title, message, type, priority);
        notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByReadFalseOrderByCreatedAtDesc();
    }

    public long getUnreadCount() {
        return notificationRepository.countUnreadNotifications();
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setRead(true);
            notification.setReadAt(java.time.LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    public void markAllAsRead() {
        List<Notification> unreadNotifications = notificationRepository.findByReadFalseOrderByCreatedAtDesc();
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(java.time.LocalDateTime.now());
        }
        notificationRepository.saveAll(unreadNotifications);
    }
}
