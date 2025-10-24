package com.rapidclean.repository;

import com.rapidclean.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByActiveTrue();
    
    @Query("SELECT s FROM Service s WHERE s.active = true ORDER BY " +
           "CASE s.name " +
           "WHEN 'Nettoyage professionnel des fast-food' THEN 1 " +
           "WHEN 'Nettoyage des salles' THEN 2 " +
           "WHEN 'Entretien sanitaires' THEN 3 " +
           "WHEN 'Nettoyage des vitres' THEN 4 " +
           "WHEN 'Nettoyage de cuisine' THEN 5 " +
           "WHEN 'Caisse et divers' THEN 6 " +
           "WHEN 'Locaux techniques et arrières' THEN 7 " +
           "WHEN 'Vestiaires et douches' THEN 8 " +
           "WHEN 'Nettoyage extérieurs' THEN 9 " +
           "WHEN 'Nettoyage des équipements de friture' THEN 10 " +
           "WHEN 'Nettoyage des systèmes de ventilation' THEN 11 " +
           "WHEN 'Nettoyage des sols industriels' THEN 12 " +
           "ELSE 99 END")
    List<Service> findByActiveTrueOrderByCustomOrder();
}
