package com.rapidclean.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact_messages")
public class ContactMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le prénom est requis")
    @Size(max = 50)
    private String firstName;
    
    @NotBlank(message = "Le nom est requis")
    @Size(max = 50)
    private String lastName;
    
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;
    
    private String phone;
    
    @NotBlank(message = "Le sujet est requis")
    @Size(max = 100)
    private String subject;
    
    @NotBlank(message = "Le message est requis")
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;
    
    private boolean read = false;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime readAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public ContactMessage() {}
    
    public ContactMessage(String firstName, String lastName, String email, String phone, 
                         String subject, String message) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.subject = subject;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public enum Status {
        NEW("Nouveau"),
        READ("Lu"),
        REPLIED("Répondu"),
        CLOSED("Fermé");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
