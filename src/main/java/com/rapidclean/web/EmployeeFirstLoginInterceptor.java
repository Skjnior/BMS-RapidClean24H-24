package com.rapidclean.web;

import com.rapidclean.entity.User;
import com.rapidclean.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;

@Component
public class EmployeeFirstLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // Only enforce for employee area
        if (path.startsWith("/employee")) {
            Principal principal = request.getUserPrincipal();
            if (principal != null) {
                User user = userRepository.findByEmail(principal.getName()).orElse(null);
                if (user != null && user.isFirstLogin()) {
                    // Allow access to the change-password page itself
                    if (!path.equals("/employee/change-password") && !path.startsWith("/employee/change-password")) {
                        response.sendRedirect(request.getContextPath() + "/employee/change-password");
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
