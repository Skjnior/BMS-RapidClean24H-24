package com.rapidclean.repository;

import com.rapidclean.entity.AuditLog;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecification {
    
    public static Specification<AuditLog> withFilters(
            Long userId,
            String action,
            String actionType,
            String resourceType,
            String ipAddress,
            String countryCode,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        return (root, query, cb) -> {
            // Charger l'entité User de manière eager pour éviter LazyInitializationException
            // Note: JOIN FETCH ne peut être utilisé que si la query n'est pas déjà un count query
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("user", JoinType.LEFT);
            }
            
            List<Predicate> predicates = new ArrayList<>();
            
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            
            if (action != null && !action.isEmpty()) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            
            if (actionType != null && !actionType.isEmpty()) {
                predicates.add(cb.equal(root.get("actionType"), actionType));
            }
            
            if (resourceType != null && !resourceType.isEmpty()) {
                predicates.add(cb.equal(root.get("resourceType"), resourceType));
            }
            
            if (ipAddress != null && !ipAddress.isEmpty()) {
                predicates.add(cb.equal(root.get("ipAddress"), ipAddress));
            }
            
            if (countryCode != null && !countryCode.isEmpty()) {
                predicates.add(cb.equal(root.get("countryCode"), countryCode));
            }
            
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

