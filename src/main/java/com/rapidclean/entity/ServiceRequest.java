package com.rapidclean.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_requests")
public class ServiceRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank(message = "Le type de service est requis")
    private String serviceType;
    
    @NotBlank(message = "L'adresse est requise")
    private String address;
    
    private String description;
    
    @NotNull(message = "La date de service est requise")
    private LocalDateTime serviceDate;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;
    
    @Positive(message = "Le prix doit être positif")
    private Double price;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public ServiceRequest() {}
    
    public ServiceRequest(User user, String serviceType, String address, String description, 
                         LocalDateTime serviceDate, Double price) {
        this.user = user;
        this.serviceType = serviceType;
        this.address = address;
        this.description = description;
        this.serviceDate = serviceDate;
        this.price = price;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDateTime serviceDate) { this.serviceDate = serviceDate; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public enum Status {
        PENDING("En attente"),
        CONFIRMED("Confirmé"),
        IN_PROGRESS("En cours"),
        COMPLETED("Terminé"),
        CANCELLED("Annulé");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
