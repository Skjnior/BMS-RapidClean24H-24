package com.rapidclean.repository;

import com.rapidclean.entity.Absence;
import com.rapidclean.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long> {
    List<Absence> findByUserOrderByAbsenceDateDesc(User user);
    List<Absence> findByUserAndAbsenceDateBetweenOrderByAbsenceDateDesc(User user, LocalDate startDate, LocalDate endDate);
    long countByUserAndAbsenceDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
