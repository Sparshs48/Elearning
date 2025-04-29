package com.example.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SessionInterceptor implements HandlerInterceptor {
    
    @Autowired
    private SessionUtil sessionUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        String path = request.getRequestURI();
        
        // Allow public endpoints
        if (path.contains("/api/register") || path.contains("/api/login")) {
            return true;
        }
        
        // Check admin access for admin endpoints
        if (path.contains("/api/admin")) {
            if (!sessionUtil.isAdmin(request.getSession())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
        }
        
        return true;
    }
}