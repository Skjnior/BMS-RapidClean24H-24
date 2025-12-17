package com.rapidclean.repository;

import com.rapidclean.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    
    /**
     * Trouve tous les messages triés par date de création (plus récents en premier)
     */
    List<ContactMessage> findAllByOrderByCreatedAtDesc();
    
    /**
     * Trouve tous les messages par statut
     */
    List<ContactMessage> findByStatus(ContactMessage.Status status);
    
    /**
     * Trouve tous les messages non lus
     */
    List<ContactMessage> findByStatusOrderByCreatedAtDesc(ContactMessage.Status status);
    
    /**
     * Compte le nombre de messages par statut
     */
    long countByStatus(ContactMessage.Status status);
    
    /**
     * Trouve les messages récents (derniers 30 jours)
     */
    @Query("SELECT cm FROM ContactMessage cm WHERE cm.createdAt >= :thirtyDaysAgo ORDER BY cm.createdAt DESC")
    List<ContactMessage> findRecentMessages(@Param("thirtyDaysAgo") java.time.LocalDateTime thirtyDaysAgo);
    
    /**
     * Trouve les messages par email
     */
    List<ContactMessage> findByEmailOrderByCreatedAtDesc(String email);
    
    /**
     * Recherche dans les messages par nom, email ou sujet
     */
    @Query("SELECT cm FROM ContactMessage cm WHERE " +
           "LOWER(cm.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cm.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cm.subject) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY cm.createdAt DESC")
    List<ContactMessage> searchMessages(String search);
    
    /**
     * Compte le nombre de messages non lus
     */
    @Query("SELECT COUNT(cm) FROM ContactMessage cm WHERE cm.read = false")
    long countUnreadMessages();
}
