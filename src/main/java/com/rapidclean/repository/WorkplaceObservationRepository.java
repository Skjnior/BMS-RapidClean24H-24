package com.rapidclean.repository;

import com.rapidclean.entity.WorkplaceObservation;
import com.rapidclean.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkplaceObservationRepository extends JpaRepository<WorkplaceObservation, Long> {
    List<WorkplaceObservation> findByUserOrderByCreatedAtDesc(User user);
    List<WorkplaceObservation> findByStatusOrderByCreatedAtDesc(WorkplaceObservation.Status status);
    List<WorkplaceObservation> findByStatusAndPriorityOrderByCreatedAtDesc(WorkplaceObservation.Status status, WorkplaceObservation.Priority priority);
    long countByStatus(WorkplaceObservation.Status status);
}
