package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.config.SessionHelper;
import com.example.demo.config.SessionManager;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {
    
	  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	
    @Autowired
    private SessionHelper sessionManager;
    
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        User user = sessionManager.getUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        
        // Check role and redirect accordingly
        switch (user.getRole()) {
            case ADMIN:
                return "redirect:/admin/dashboard";
            case PROFESSOR:
                return "redirect:/professor/dashboard";
            default:
                return "dashboard";  // Student dashboard
        }
    }
    
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session) {
        if (!sessionManager.isAdmin(session)) {
            return "redirect:/dashboard";
        }
        return "admin/dashboard";
    }
    
    @GetMapping("/admin/courses")
    public String manageCourses(HttpSession session) {
        if (!sessionManager.isAdmin(session)) {
            return "redirect:/dashboard";
        }
        return "admin/courses";  // This will render courses.html from templates/admin/
    }
    
    @GetMapping("/professor/dashboard")
    public String professorDashboard(HttpSession session) {
        User user = sessionManager.getUser(session);
        if (user == null || user.getRole() != UserRole.PROFESSOR) {
            return "redirect:/login";
        }
        try {
            return "professor/dashboard";
        } catch (Exception e) {
            logger.error("Error loading professor dashboard: ", e);
            return "error";
        }
    }
    
    @GetMapping("/custom-error")
    public String handleCustomError() {
        return "error"; // Redirect to the custom error page
    }
}