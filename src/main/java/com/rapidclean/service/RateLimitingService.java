package com.rapidclean.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de rate limiting simple basé sur la mémoire
 * Pour une production réelle, utilisez Redis ou un système distribué
 */
@Service
public class RateLimitingService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingService.class);
    
    // Limite: nombre de requêtes autorisées
    private static final int MAX_REQUESTS = 5;
    
    // Fenêtre de temps en minutes
    private static final int TIME_WINDOW_MINUTES = 15;
    
    // Map pour stocker les tentatives: clé = identifiant (IP ou email), valeur = liste des timestamps
    private final Map<String, RequestTracker> requestMap = new ConcurrentHashMap<>();
    
    /**
     * Vérifie si une requête est autorisée
     * @param identifier Identifiant unique (IP, email, etc.)
     * @return true si autorisée, false sinon
     */
    public boolean isAllowed(String identifier) {
        RequestTracker tracker = requestMap.get(identifier);
        LocalDateTime now = LocalDateTime.now();
        
        if (tracker == null) {
            // Première requête
            tracker = new RequestTracker();
            tracker.addRequest(now);
            requestMap.put(identifier, tracker);
            return true;
        }
        
        // Nettoyer les anciennes requêtes (hors fenêtre de temps)
        tracker.cleanOldRequests(now.minusMinutes(TIME_WINDOW_MINUTES));
        
        // Vérifier si la limite est atteinte
        if (tracker.getRequestCount() >= MAX_REQUESTS) {
            logger.warn("Rate limit exceeded for: {}", identifier);
            return false;
        }
        
        // Ajouter la nouvelle requête
        tracker.addRequest(now);
        return true;
    }
    
    /**
     * Réinitialise le compteur pour un identifiant (après succès de validation)
     */
    public void reset(String identifier) {
        requestMap.remove(identifier);
    }
    
    /**
     * Classe interne pour tracker les requêtes
     */
    private static class RequestTracker {
        private java.util.List<LocalDateTime> requests = new java.util.ArrayList<>();
        
        public void addRequest(LocalDateTime timestamp) {
            requests.add(timestamp);
        }
        
        public void cleanOldRequests(LocalDateTime cutoff) {
            requests.removeIf(timestamp -> timestamp.isBefore(cutoff));
        }
        
        public int getRequestCount() {
            return requests.size();
        }
    }
}



