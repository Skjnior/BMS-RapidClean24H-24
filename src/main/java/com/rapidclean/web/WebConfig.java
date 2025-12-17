package com.rapidclean.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private EmployeeFirstLoginInterceptor firstLoginInterceptor;
    
    @Autowired
    private AuditInterceptor auditInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Intercepteur pour forcer le changement de mot de passe des employés
        registry.addInterceptor(firstLoginInterceptor)
                .addPathPatterns("/employee/**")
                .excludePathPatterns("/employee/change-password", "/employee-login", "/css/**", "/js/**", "/images/**");
        
        // Intercepteur d'audit - capture toutes les actions
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error");
    }
}
