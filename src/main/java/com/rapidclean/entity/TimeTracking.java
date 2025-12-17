package com.rapidclean.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "time_tracking")
public class TimeTracking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate trackingDate;
    
    private LocalTime arrivalTime;
    
    private LocalTime departureTime;
    
    private String notes;
    
    // Constructors
    public TimeTracking() {}
    
    public TimeTracking(User user, LocalDate trackingDate) {
        this.user = user;
        this.trackingDate = trackingDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public LocalDate getTrackingDate() { return trackingDate; }
    public void setTrackingDate(LocalDate trackingDate) { this.trackingDate = trackingDate; }
    
    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
    
    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public java.time.Duration getDuration() {
        if (arrivalTime == null || departureTime == null) {
            return null;
        }
        return java.time.Duration.between(arrivalTime, departureTime);
    }
    
    public String getFormattedDuration() {
        java.time.Duration duration = getDuration();
        if (duration == null) {
            return "--:--";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}
