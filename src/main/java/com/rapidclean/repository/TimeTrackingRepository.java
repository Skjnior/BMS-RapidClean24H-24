package com.rapidclean.repository;

import com.rapidclean.entity.TimeTracking;
import com.rapidclean.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeTrackingRepository extends JpaRepository<TimeTracking, Long> {
    List<TimeTracking> findByUserOrderByTrackingDateDesc(User user);
    Optional<TimeTracking> findByUserAndTrackingDate(User user, LocalDate trackingDate);
    List<TimeTracking> findByUserAndTrackingDateBetweenOrderByTrackingDateDesc(User user, LocalDate startDate, LocalDate endDate);
}
