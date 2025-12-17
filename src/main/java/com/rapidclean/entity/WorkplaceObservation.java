package com.rapidclean.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "workplace_observations")
public class WorkplaceObservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    private String photoPath; // Stocke les chemins séparés par des virgules pour plusieurs images
    
    @Enumerated(EnumType.STRING)
    private Priority priority;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime resolvedAt;
    
    private String adminNotes;
    
    // Helper methods pour gérer plusieurs images
    public java.util.List<String> getPhotoPaths() {
        if (photoPath == null || photoPath.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        // Split par virgule et trimmer les espaces
        return java.util.Arrays.stream(photoPath.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(java.util.stream.Collectors.toList());
    }
    
    public void setPhotoPaths(java.util.List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            this.photoPath = null;
        } else {
            this.photoPath = String.join(",", paths);
        }
    }
    
    public void addPhotoPath(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        if (photoPath == null || photoPath.isEmpty()) {
            photoPath = path;
        } else {
            photoPath += "," + path;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = Status.PENDING;
        }
    }
    
    // Constructors
    public WorkplaceObservation() {}
    
    public WorkplaceObservation(User user, String title, String description) {
        this.user = user;
        this.title = title;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    
    public enum Priority {
        LOW("Faible"),
        MEDIUM("Moyen"),
        HIGH("Élevé"),
        CRITICAL("Critique");
        
        private final String label;
        
        Priority(String label) {
            this.label = label;
        }
        
        public String getLabel() { return label; }
    }
    
    public enum Status {
        PENDING("En Attente"),
        IN_PROGRESS("En Cours"),
        RESOLVED("Résolu"),
        CLOSED("Fermé");
        
        private final String label;
        
        Status(String label) {
            this.label = label;
        }
        
        public String getLabel() { return label; }
    }
}
