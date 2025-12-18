package com.rapidclean.repository;

import com.rapidclean.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    
    /**
     * Compte les logs récents depuis une date donnée
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp >= :since")
    long countRecentLogs(@Param("since") LocalDateTime since);
    
    /**
     * Trouve les actions les plus fréquentes
     */
    @Query("SELECT a.action, COUNT(a) as count FROM AuditLog a GROUP BY a.action ORDER BY count DESC")
    List<Object[]> findMostFrequentActions(Pageable pageable);
    
    /**
     * Trouve les pays les plus actifs
     */
    @Query("SELECT a.countryCode, COUNT(a) as count FROM AuditLog a WHERE a.countryCode IS NOT NULL GROUP BY a.countryCode ORDER BY count DESC")
    List<Object[]> findMostActiveCountries(Pageable pageable);
    
    /**
     * Trouve les utilisateurs les plus actifs
     */
    @Query("SELECT a.user.id, a.user.email, COUNT(a) as count FROM AuditLog a WHERE a.user IS NOT NULL GROUP BY a.user.id, a.user.email ORDER BY count DESC")
    List<Object[]> findMostActiveUsers(Pageable pageable);
    
    /**
     * Trouve les IPs les plus actives
     */
    @Query("SELECT a.ipAddress, COUNT(a) as count FROM AuditLog a WHERE a.ipAddress IS NOT NULL GROUP BY a.ipAddress ORDER BY count DESC")
    List<Object[]> findMostActiveIPs(Pageable pageable);
    
    /**
     * Trouve les statistiques quotidiennes
     */
    @Query(value = "SELECT DATE(timestamp) as date, COUNT(*) as count FROM audit_logs WHERE timestamp >= CURRENT_DATE - make_interval(days => :days) GROUP BY DATE(timestamp) ORDER BY date DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Object[]> findDailyStatistics(@Param("days") int days, @Param("limit") int limit, @Param("offset") int offset);
    
    /**
     * Supprime les logs antérieurs à une date donnée
     */
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    void deleteByTimestampBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Supprime les logs dans une période donnée
     */
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate")
    void deleteByTimestampBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Trouve les logs d'un utilisateur triés par timestamp décroissant
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Trouve un log par ID avec l'utilisateur chargé (pour éviter LazyInitializationException)
     */
    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.user WHERE a.id = :id")
    java.util.Optional<AuditLog> findByIdWithUser(@Param("id") Long id);
    
    /**
     * Supprime les logs par leurs IDs
     */
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.id IN :ids")
    void deleteByIds(@Param("ids") List<Long> ids);
}

