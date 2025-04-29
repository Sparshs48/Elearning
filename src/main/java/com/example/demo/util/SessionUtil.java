package com.example.demo.util;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;

@Component
public class SessionUtil {
    private static final String USER_SESSION_KEY = "currentUser";
    
    public void setUserInSession(HttpSession session, User user) {
        session.setAttribute(USER_SESSION_KEY, user);
    }
    
    public User getUserFromSession(HttpSession session) {
        return (User) session.getAttribute(USER_SESSION_KEY);
    }
    
    public void removeUserFromSession(HttpSession session) {
        session.removeAttribute(USER_SESSION_KEY);
    }
    
    public boolean isAdmin(HttpSession session) {
        User user = getUserFromSession(session);
        return user != null && user.getRole() == UserRole.ADMIN;
    }
}