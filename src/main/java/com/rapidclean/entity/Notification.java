package com.rapidclean.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le titre est requis")
    private String title;
    
    @NotBlank(message = "Le message est requis")
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    private Type type;
    
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;
    
    private boolean read = false;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime readAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public Notification() {}
    
    public Notification(String title, String message, Type type, Priority priority) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    
    public enum Type {
        NEW_MESSAGE("Nouveau Message"),
        NEW_REVIEW("Nouvel Avis"),
        NEW_REQUEST("Nouvelle Demande"),
        SYSTEM("Système");
        
        private final String displayName;
        
        Type(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum Priority {
        LOW("Faible"),
        MEDIUM("Moyenne"),
        HIGH("Élevée"),
        URGENT("Urgente");
        
        private final String displayName;
        
        Priority(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
