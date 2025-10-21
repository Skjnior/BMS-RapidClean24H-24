package com.rapidclean.repository;

import com.rapidclean.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByApprovedTrueOrderByCreatedAtDesc();
    
    List<Review> findByApprovedFalseOrderByCreatedAtDesc();
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.approved = true")
    Double getAverageRating();
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.approved = true")
    long countApprovedReviews();
}
