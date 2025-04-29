package com.example.demo.controller;

import java.time.LocalDateTime;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.config.SessionHelper;
import com.example.demo.config.SessionManager;
import com.example.demo.model.LoginRequest;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class UserController {
    
	  private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    
    @Autowired
    private SessionHelper sessionHelper;
    
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(@RequestBody User user) {
        try {
            User registeredUser = userService.registerStudent(user);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, 
                                 HttpSession session) {
        try {
            User user = userService.login(loginRequest);
            sessionHelper.setUser(session, user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
        }
    }

    @PostMapping("/register/professor")
    public ResponseEntity<?> createProfessor(@RequestBody User newUser, 
                                           HttpSession session) {
        if (!sessionHelper.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Admin access required");
        }

        try {
            if (!UserRole.PROFESSOR.equals(newUser.getRole())) {
                return ResponseEntity.badRequest()
                    .body("Can only create PROFESSOR accounts");
            }
            
            // Additional validation specific to professor creation
            if (newUser.getPassword() == null || newUser.getEmail() == null) {
                return ResponseEntity.badRequest()
                    .body("Password and email are required for professor accounts");
            }
            
            User createdUser = userService.createUser(newUser);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            logger.error("Error creating professor: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/session/check")
    public ResponseEntity<?> checkSession(HttpSession session) {
        User user = sessionHelper.getUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("No active session");
        }
        return ResponseEntity.ok(Map.of(
            "user", user,
            "isAdmin", sessionHelper.isAdmin(session)
        ));
    }
    
    @GetMapping("/manage/users")
    public ResponseEntity<?> getUsers(
        @RequestParam(required = false) String role,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        HttpSession session) {
        
        try {
            Page<User> userPage;
            if (role != null) {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                userPage = userService.getUsersByRolePaginated(userRole, page, size);
            } else if (search != null) {
                userPage = userService.searchUsersPaginated(search, page, size);
            } else {
                userPage = userService.getAllUsersPaginated(page, size);
            }
            
            return ResponseEntity.ok(userPage);
        } catch (Exception e) {
            logger.error("Error loading users: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error loading users: " + e.getMessage());
        }
    }
    
    @PutMapping("/manage/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id, HttpSession session) {
        logger.info("Attempting to deactivate user with ID: {}", id);
        
        if (!sessionHelper.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Admin access required");
        }

        try {
            User deactivatedUser = userService.deactivateUser(id);
            return ResponseEntity.ok(deactivatedUser);
        } catch (Exception e) {
            logger.error("Error deactivating user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deactivating user: " + e.getMessage());
        }
    }
    
    @PutMapping("/manage/users/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id, HttpSession session) {
        logger.info("Attempting to activate user with ID: {}", id);
        
        if (!sessionHelper.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Admin access required");
        }

        try {
            User activatedUser = userService.activateUser(id);
            return ResponseEntity.ok(activatedUser);
        } catch (Exception e) {
            logger.error("Error activating user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error activating user: " + e.getMessage());
        }
    }
}