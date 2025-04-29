package com.example.demo.config;

import org.springframework.stereotype.Component;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;

import jakarta.servlet.http.HttpSession;

@Component
public class SessionHelper {
    private static final String USER_KEY = "LOGGED_IN_USER";
    
    public void setUser(HttpSession session, User user) {
        session.setAttribute(USER_KEY, user);
    }
    
    public User getUser(HttpSession session) {
        return (User) session.getAttribute(USER_KEY);
    }
    
    public boolean isAdmin(HttpSession session) {
        User user = getUser(session);
        return user != null && UserRole.ADMIN.equals(user.getRole());
    }
    
    public void clearSession(HttpSession session) {
        session.removeAttribute(USER_KEY);
        session.invalidate();
    }
}