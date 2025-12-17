package com.rapidclean.service;

/**
 * Classe pour capturer les informations de la requête HTTP avant traitement asynchrone
 */
public class AuditRequestInfo {
    private String method;
    private String requestUrl;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String referer;
    private String acceptLanguage;
    private String queryString;
    
    public AuditRequestInfo() {}
    
    public AuditRequestInfo(String method, String requestUrl, String ipAddress, 
                           String userAgent, String sessionId, String referer, 
                           String acceptLanguage, String queryString) {
        this.method = method;
        this.requestUrl = requestUrl;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.sessionId = sessionId;
        this.referer = referer;
        this.acceptLanguage = acceptLanguage;
        this.queryString = queryString;
    }
    
    // Getters and Setters
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getRequestUrl() {
        return requestUrl;
    }
    
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getReferer() {
        return referer;
    }
    
    public void setReferer(String referer) {
        this.referer = referer;
    }
    
    public String getAcceptLanguage() {
        return acceptLanguage;
    }
    
    public void setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }
    
    public String getQueryString() {
        return queryString;
    }
    
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
}



