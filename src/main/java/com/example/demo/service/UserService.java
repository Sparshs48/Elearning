package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.dao.UserDAO;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;

import com.example.demo.model.LoginRequest;
import com.example.demo.model.UserStatus;

@Service
public class UserService {
    
    @Autowired
    private UserDAO userDAO;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerStudent(User user) {
        // Validate input
        validateUserInput(user);
        
        // Check for existing username
        if (userDAO.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        return userDAO.save(user);
    }
    
    private void validateUserInput(User user) {
        if (user.getUsername() == null || user.getUsername().length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters");
        }
        
        if (user.getEmail() == null || !user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new RuntimeException("Invalid email format");
        }
        
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        // Additional validation for professors
        if (UserRole.PROFESSOR.equals(user.getRole())) {
            if (!user.getEmail().endsWith(".edu")) {
                throw new RuntimeException("Professor email must be an educational email (.edu)");
            }
            
            if (user.getPassword().length() < 8) {
                throw new RuntimeException("Professor password must be at least 8 characters");
            }
            
            if (!user.getPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$")) {
                throw new RuntimeException("Professor password must contain at least one digit, one lowercase, one uppercase, and one special character");
            }
        }
    }
    public User createUser(User newUser) {
        // Validate input first
        validateUserInput(newUser);
        
        // Check for existing username
        if (userDAO.findByUsername(newUser.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check for existing email
        if (userDAO.findByEmail(newUser.getEmail()) != null) {
            throw new RuntimeException("Email already registered");
        }
        
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setCreatedAt(LocalDateTime.now());
        return userDAO.save(newUser);
    }
    
    public User login(LoginRequest request) {
        User user = userDAO.findByUsername(request.getUsername());
        
        // Add exact username match check
        if (user == null || !user.getUsername().equals(request.getUsername())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Account is inactive");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        return user;
    }
    
    public List<User> getAllUsers() {
        return userDAO.findAllUsers();
    }

    public List<User> getUsersByRole(UserRole role) {
        return userDAO.findUsersByRole(role);
    }

    public List<User> searchUsers(String searchTerm) {
        return userDAO.searchUsers(searchTerm);
    }
    
    public User deactivateUser(Long userId) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        // Prevent admin deletion
        if (UserRole.ADMIN.equals(user.getRole())) {
            throw new RuntimeException("Admin users cannot be deactivated");
        }
        
        user.setStatus(UserStatus.UNACTIVE);
        return userDAO.save(user);
    }
    
    public User activateUser(Long userId) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.setStatus(UserStatus.ACTIVE);
        return userDAO.save(user);
    }
    
    public List<User> getActiveUsers() {
        return userDAO.findUsersByStatus(UserStatus.ACTIVE);
    }
    
    public List<User> getInactiveUsers() {
        return userDAO.findUsersByStatus(UserStatus.UNACTIVE);
    }
    
    public User getUserById(Long id) {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new RuntimeException("User not found with id: " + id);
        }
        return user;
    }
    public Page<User> getAllUsersPaginated(int page, int size) {
        return userDAO.findAllPaginated(page, size);
    }
    
    public Page<User> getUsersByRolePaginated(UserRole role, int page, int size) {
        return userDAO.findByRolePaginated(role, page, size);
    }
    
    public Page<User> searchUsersPaginated(String searchTerm, int page, int size) {
        return userDAO.searchPaginated(searchTerm, page, size);
    }
    
}