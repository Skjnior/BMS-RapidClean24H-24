package com.rapidclean.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "absences")
public class Absence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate absenceDate;
    
    @Enumerated(EnumType.STRING)
    private AbsenceType type;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public Absence() {}
    
    public Absence(User user, LocalDate absenceDate, AbsenceType type) {
        this.user = user;
        this.absenceDate = absenceDate;
        this.type = type;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public LocalDate getAbsenceDate() { return absenceDate; }
    public void setAbsenceDate(LocalDate absenceDate) { this.absenceDate = absenceDate; }
    
    public AbsenceType getType() { return type; }
    public void setType(AbsenceType type) { this.type = type; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public enum AbsenceType {
        SICK_LEAVE("Congé Maladie"),
        PAID_LEAVE("Congé Payé"),
        UNPAID_LEAVE("Congé Non Payé"),
        HOLIDAY("Jour Férié"),
        SPECIAL_LEAVE("Congé Spécial");
        
        private final String label;
        
        AbsenceType(String label) {
            this.label = label;
        }
        
        public String getLabel() { return label; }
    }
}
