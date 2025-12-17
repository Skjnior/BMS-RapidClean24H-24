package com.rapidclean.repository;

import com.rapidclean.entity.AuditLog;
import com.rapidclean.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    
    // Trouver tous les logs d'un utilisateur
    @EntityGraph(attributePaths = {"user"})
    Page<AuditLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);
    
    // Trouver tous les logs d'un utilisateur par ID
    @EntityGraph(attributePaths = {"user"})
    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    // Trouver les logs par action
    Page<AuditLog> findByActionOrderByTimestampDesc(String action, Pageable pageable);
    
    // Trouver les logs par type d'action
    Page<AuditLog> findByActionTypeOrderByTimestampDesc(String actionType, Pageable pageable);
    
    // Trouver les logs par type de ressource
    Page<AuditLog> findByResourceTypeOrderByTimestampDesc(String resourceType, Pageable pageable);
    
    // Trouver les logs par IP
    Page<AuditLog> findByIpAddressOrderByTimestampDesc(String ipAddress, Pageable pageable);
    
    // Trouver les logs par pays
    Page<AuditLog> findByCountryOrderByTimestampDesc(String country, Pageable pageable);
    
    // Trouver les logs par code pays
    Page<AuditLog> findByCountryCodeOrderByTimestampDesc(String countryCode, Pageable pageable);
    
    // Trouver les logs par rôle utilisateur
    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.user WHERE a.user.role = :role ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUserRoleOrderByTimestampDesc(@Param("role") User.Role role, Pageable pageable);
    
    // Trouver les logs dans une période
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );
    
    // Note: findWithFilters est maintenant implémenté via Specification dans AuditLogSpecification
    // Utiliser findAll(Specification, Pageable) à la place
    
    // Compter les logs par action
    long countByAction(String action);
    
    // Compter les logs par utilisateur
    long countByUser(User user);
    
    // Compter les logs par pays
    long countByCountryCode(String countryCode);
    
    // Trouver les actions les plus fréquentes
    @Query("SELECT a.action, COUNT(a) as count FROM AuditLog a GROUP BY a.action ORDER BY count DESC")
    List<Object[]> findMostFrequentActions(Pageable pageable);
    
    // Trouver les pays les plus actifs
    @Query("SELECT a.country, a.countryCode, COUNT(a) as count FROM AuditLog a WHERE a.country IS NOT NULL GROUP BY a.country, a.countryCode ORDER BY count DESC")
    List<Object[]> findMostActiveCountries(Pageable pageable);
    
    // Trouver les utilisateurs les plus actifs
    @Query("SELECT a.user.id, a.user.email, COUNT(a) as count FROM AuditLog a WHERE a.user IS NOT NULL GROUP BY a.user.id, a.user.email ORDER BY count DESC")
    List<Object[]> findMostActiveUsers(Pageable pageable);
    
    // Trouver les IPs les plus actives
    @Query("SELECT a.ipAddress, COUNT(a) as count FROM AuditLog a WHERE a.ipAddress IS NOT NULL GROUP BY a.ipAddress ORDER BY count DESC")
    List<Object[]> findMostActiveIPs(Pageable pageable);
    
    // Statistiques par jour (compatible H2 et PostgreSQL)
    @Query(value = "SELECT CAST(timestamp AS DATE) as date, COUNT(*) as count FROM audit_logs GROUP BY CAST(timestamp AS DATE) ORDER BY date DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Object[]> findDailyStatistics(@Param("limit") int limit, @Param("offset") int offset);
    
    // Supprimer les logs anciens (pour nettoyage)
    void deleteByTimestampBefore(LocalDateTime date);
    
    // Supprimer les logs dans une période spécifique
    void deleteByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Compter les logs récents (dernières 24h)
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp >= :since")
    long countRecentLogs(@Param("since") LocalDateTime since);
}

