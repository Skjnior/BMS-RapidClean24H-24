package com.rapidclean.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_ip", columnList = "ip_address")
})
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    
    @Column(nullable = false, length = 100)
    private String action; // Ex: "LOGIN", "LOGOUT", "CREATE_SERVICE", "UPDATE_USER", etc.
    
    @Column(name = "action_type", length = 50)
    private String actionType; // Ex: "AUTHENTICATION", "CRUD", "VIEW", "DOWNLOAD", etc.
    
    @Column(name = "resource_type", length = 100)
    private String resourceType; // Ex: "User", "Service", "ServiceRequest", etc.
    
    @Column(name = "resource_id")
    private Long resourceId; // ID de la ressource affectée
    
    @Column(name = "request_method", length = 10)
    private String requestMethod; // GET, POST, PUT, DELETE
    
    @Column(name = "request_url", length = 500)
    private String requestUrl;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "browser", length = 100)
    private String browser; // Chrome, Firefox, Safari, etc.
    
    @Column(name = "browser_version", length = 50)
    private String browserVersion;
    
    @Column(name = "operating_system", length = 100)
    private String operatingSystem; // Windows, macOS, Linux, Android, iOS
    
    @Column(name = "device_type", length = 50)
    private String deviceType; // Desktop, Mobile, Tablet
    
    @Column(name = "country", length = 100)
    private String country;
    
    @Column(name = "country_code", length = 10)
    private String countryCode; // FR, US, etc.
    
    @Column(name = "city", length = 100)
    private String city;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "session_id", length = 200)
    private String sessionId;
    
    @Column(name = "status_code")
    private Integer statusCode; // Code HTTP de réponse (200, 404, 500, etc.)
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs; // Temps de réponse en millisecondes
    
    @Column(name = "request_size_bytes")
    private Long requestSizeBytes;
    
    @Column(name = "response_size_bytes")
    private Long responseSizeBytes;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams; // Paramètres de la requête (JSON)
    
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody; // Corps de la requête (pour POST/PUT)
    
    @Column(name = "referer", length = 500)
    private String referer;
    
    @Column(name = "accept_language", length = 100)
    private String acceptLanguage;
    
    @Column(name = "timezone", length = 50)
    private String timezone;
    
    @Column(name = "screen_resolution", length = 50)
    private String screenResolution; // Ex: "1920x1080"
    
    @Column(name = "is_mobile")
    private Boolean isMobile;
    
    @Column(name = "is_bot")
    private Boolean isBot;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // Détails supplémentaires en JSON
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    // Constructors
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }
    
    public AuditLog(User user, String action, String requestMethod, String requestUrl) {
        this();
        this.user = user;
        this.action = action;
        this.requestMethod = requestMethod;
        this.requestUrl = requestUrl;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    
    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    
    public String getRequestUrl() { return requestUrl; }
    public void setRequestUrl(String requestUrl) { this.requestUrl = requestUrl; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    
    public String getBrowserVersion() { return browserVersion; }
    public void setBrowserVersion(String browserVersion) { this.browserVersion = browserVersion; }
    
    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }
    
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    
    public Long getRequestSizeBytes() { return requestSizeBytes; }
    public void setRequestSizeBytes(Long requestSizeBytes) { this.requestSizeBytes = requestSizeBytes; }
    
    public Long getResponseSizeBytes() { return responseSizeBytes; }
    public void setResponseSizeBytes(Long responseSizeBytes) { this.responseSizeBytes = responseSizeBytes; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getRequestParams() { return requestParams; }
    public void setRequestParams(String requestParams) { this.requestParams = requestParams; }
    
    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    
    public String getReferer() { return referer; }
    public void setReferer(String referer) { this.referer = referer; }
    
    public String getAcceptLanguage() { return acceptLanguage; }
    public void setAcceptLanguage(String acceptLanguage) { this.acceptLanguage = acceptLanguage; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public String getScreenResolution() { return screenResolution; }
    public void setScreenResolution(String screenResolution) { this.screenResolution = screenResolution; }
    
    public Boolean getIsMobile() { return isMobile; }
    public void setIsMobile(Boolean isMobile) { this.isMobile = isMobile; }
    
    public Boolean getIsBot() { return isBot; }
    public void setIsBot(Boolean isBot) { this.isBot = isBot; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    // Helper methods
    public String getUserDisplayName() {
        if (user != null) {
            return user.getFullName() + " (" + user.getEmail() + ")";
        }
        return "Anonyme";
    }
    
    public String getRoleDisplay() {
        if (user != null && user.getRole() != null) {
            return user.getRole().name();
        }
        return "ANONYMOUS";
    }
}



