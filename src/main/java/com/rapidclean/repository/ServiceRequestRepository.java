package com.rapidclean.repository;

import com.rapidclean.entity.ServiceRequest;
import com.rapidclean.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByUser(User user);
    List<ServiceRequest> findByStatus(ServiceRequest.Status status);
    
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.serviceDate BETWEEN :startDate AND :endDate")
    List<ServiceRequest> findByServiceDateBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.status = :status")
    long countByStatus(@Param("status") ServiceRequest.Status status);
}
