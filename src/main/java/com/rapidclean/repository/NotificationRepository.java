package com.rapidclean.repository;

import com.rapidclean.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReadFalseOrderByCreatedAtDesc();
    
    List<Notification> findByTypeOrderByCreatedAtDesc(Notification.Type type);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.read = false")
    long countUnreadNotifications();
}
