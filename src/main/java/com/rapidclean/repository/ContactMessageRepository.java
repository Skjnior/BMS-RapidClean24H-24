package com.rapidclean.repository;

import com.rapidclean.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findByStatusOrderByCreatedAtDesc(ContactMessage.Status status);
    
    List<ContactMessage> findByReadFalseOrderByCreatedAtDesc();
    
    @Query("SELECT COUNT(c) FROM ContactMessage c WHERE c.read = false")
    long countUnreadMessages();
    
    @Query("SELECT COUNT(c) FROM ContactMessage c WHERE c.status = 'NEW'")
    long countNewMessages();
}
