package com.rapidclean.repository;

import com.rapidclean.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @EntityGraph(attributePaths = {"serviceRequests"})
    List<User> findAll();
    
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
   
}
