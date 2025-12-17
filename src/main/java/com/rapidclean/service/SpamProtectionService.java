package com.rapidclean.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service de protection anti-spam
 */
@Service
public class SpamProtectionService {

    private static final Logger logger = LoggerFactory.getLogger(SpamProtectionService.class);
    
    // Mots-clés spam courants
    private static final List<String> SPAM_KEYWORDS = Arrays.asList(
        "viagra", "casino", "poker", "loan", "debt", "free money",
        "click here", "buy now", "limited offer", "act now",
        "urgent", "winner", "prize", "lottery"
    );
    
    // Pattern pour détecter trop de liens
    private static final Pattern LINK_PATTERN = Pattern.compile("https?://", Pattern.CASE_INSENSITIVE);
    
    // Pattern pour détecter trop de caractères répétitifs
    private static final Pattern REPEAT_PATTERN = Pattern.compile("(.)\\1{10,}");
    
    /**
     * Vérifie si un message est du spam
     * @param content Contenu du message
     * @param email Email de l'expéditeur (optionnel)
     * @return true si c'est probablement du spam
     */
    public boolean isSpam(String content, String email) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        String lowerContent = content.toLowerCase();
        
        // Vérifier les mots-clés spam
        for (String keyword : SPAM_KEYWORDS) {
            if (lowerContent.contains(keyword)) {
                logger.warn("Spam detected: keyword '{}' found", keyword);
                return true;
            }
        }
        
        // Vérifier trop de liens (plus de 3 liens)
        long linkCount = LINK_PATTERN.matcher(content).results().count();
        if (linkCount > 3) {
            logger.warn("Spam detected: too many links ({})", linkCount);
            return true;
        }
        
        // Vérifier caractères répétitifs
        if (REPEAT_PATTERN.matcher(content).find()) {
            logger.warn("Spam detected: repeating characters");
            return true;
        }
        
        // Vérifier longueur excessive (probablement copier-coller)
        if (content.length() > 5000) {
            logger.warn("Spam detected: content too long ({})", content.length());
            return true;
        }
        
        // Vérifier email suspect (si fourni)
        if (email != null && isSuspiciousEmail(email)) {
            logger.warn("Spam detected: suspicious email ({})", email);
            return true;
        }
        
        return false;
    }
    
    /**
     * Vérifie si un email est suspect
     */
    private boolean isSuspiciousEmail(String email) {
        if (email == null) {
            return false;
        }
        
        String lowerEmail = email.toLowerCase();
        
        // Liste de domaines suspects (à adapter selon vos besoins)
        List<String> suspiciousDomains = Arrays.asList(
            "tempmail", "throwaway", "10minutemail", "guerrillamail"
        );
        
        for (String domain : suspiciousDomains) {
            if (lowerEmail.contains(domain)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Vérifie le champ honeypot (doit être vide)
     */
    public boolean isHoneypotValid(String honeypotValue) {
        return honeypotValue == null || honeypotValue.trim().isEmpty();
    }
}



