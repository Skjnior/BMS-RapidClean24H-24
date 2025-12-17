package com.rapidclean.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire global des exceptions pour l'application
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Gestion des erreurs de validation (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationExceptions(MethodArgumentNotValidException ex, RedirectAttributes redirectAttributes) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String errorMessage = errors.values().stream()
            .collect(Collectors.joining(", "));
        
        redirectAttributes.addFlashAttribute("error", "Erreur de validation : " + errorMessage);
        logger.warn("Validation error: {}", errors);
        return "redirect:/error";
    }

    /**
     * Gestion des erreurs de validation (Binding)
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBindException(BindException ex, RedirectAttributes redirectAttributes) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String errorMessage = errors.values().stream()
            .collect(Collectors.joining(", "));
        
        redirectAttributes.addFlashAttribute("error", "Erreur de validation : " + errorMessage);
        logger.warn("Binding error: {}", errors);
        return "redirect:/error";
    }

    /**
     * Gestion des violations de contraintes
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolationException(ConstraintViolationException ex, RedirectAttributes redirectAttributes) {
        String errorMessage = ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
        
        redirectAttributes.addFlashAttribute("error", "Erreur de validation : " + errorMessage);
        logger.warn("Constraint violation: {}", errorMessage);
        return "redirect:/error";
    }

    /**
     * Gestion des erreurs d'accès refusé
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDeniedException(AccessDeniedException ex, Model model) {
        logger.warn("Access denied: {}", ex.getMessage());
        model.addAttribute("error", "Accès refusé. Vous n'avez pas les permissions nécessaires.");
        model.addAttribute("status", 403);
        return "error";
    }

    /**
     * Gestion des erreurs de taille de fichier trop grande
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, RedirectAttributes redirectAttributes) {
        logger.warn("File upload size exceeded: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", 
            "Le fichier est trop volumineux. Taille maximale autorisée : " + 
            (ex.getMaxUploadSize() / 1024 / 1024) + " MB");
        return "redirect:/error";
    }

    /**
     * Gestion des IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex, RedirectAttributes redirectAttributes) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "Erreur : " + ex.getMessage());
        return "redirect:/error";
    }

    /**
     * Gestion des ressources non trouvées (404)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFoundException(NoResourceFoundException ex, Model model) {
        // Ignorer silencieusement les requêtes .well-known (Chrome DevTools, etc.)
        if (ex.getResourcePath() != null && ex.getResourcePath().startsWith(".well-known")) {
            return null;
        }
        
        logger.debug("Resource not found: {}", ex.getResourcePath());
        model.addAttribute("status", 404);
        model.addAttribute("error", "La page que vous recherchez n'existe pas ou a été déplacée.");
        model.addAttribute("path", ex.getResourcePath());
        return "error";
    }

    /**
     * Gestion des erreurs génériques
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model, RedirectAttributes redirectAttributes) {
        // Ignorer les NoResourceFoundException déjà gérées
        if (ex instanceof NoResourceFoundException) {
            return handleNoResourceFoundException((NoResourceFoundException) ex, model);
        }
        
        logger.error("Unexpected error occurred", ex);
        
        // En production, ne pas exposer les détails de l'erreur
        String errorMessage = "Une erreur inattendue s'est produite. Veuillez réessayer plus tard.";
        
        // En développement, afficher plus de détails
        if (logger.isDebugEnabled()) {
            errorMessage = "Erreur : " + ex.getMessage();
        }
        
        redirectAttributes.addFlashAttribute("error", errorMessage);
        model.addAttribute("error", errorMessage);
        model.addAttribute("status", 500);
        return "error";
    }
}

