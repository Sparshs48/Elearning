package com.example.demo.config;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;

import jakarta.servlet.http.HttpSession;

@Component
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static final String USER_ATTRIBUTE = "USER";

    public void createSession(HttpSession session, User user) {
        logger.info("Creating session for user: {} with role: {}", 
            user.getUsername(), user.getRole());
            
        session.setAttribute(USER_ATTRIBUTE, user);
        
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        
        Authentication auth = new UsernamePasswordAuthenticationToken(
            user.getUsername(), null, authorities);
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
            context
        );
        
        logger.info("Session created with authorities: {}", authorities);
    }

    public User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute(USER_ATTRIBUTE);
    }

    public boolean isAdmin(HttpSession session) {
        User user = getCurrentUser(session);
        boolean isAdmin = user != null && UserRole.ADMIN.equals(user.getRole());
        logger.info("Checking admin status for user: {}, isAdmin: {}", 
            user != null ? user.getUsername() : "null", isAdmin);
        return isAdmin;
    }
}