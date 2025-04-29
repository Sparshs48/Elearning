package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.controller.UserController;
import com.example.demo.dao.AssignmentDAO;
import com.example.demo.model.Assignment;
import com.example.demo.model.Course;
import com.example.demo.model.User;

@Service
public class AssignmentService {
	
	  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private AssignmentDAO assignmentDao;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private CourseService courseService;

    public Assignment createAssignment(Assignment assignment, User professor) {
        try {
            Course course = courseService.getCourseByProfessor(professor);
            if (course == null) {
                throw new RuntimeException("No course found for professor");
            }
            
            assignment.setCreatedAt(LocalDateTime.now());
            assignment.setCourse(course);
            
            // Save assignment first
            Assignment savedAssignment = assignmentDao.save(assignment);
            
            // Update course's assignment list
            course.getAssignments().add(savedAssignment);
            courseService.updateCourse(course);
            
            return savedAssignment;
        } catch (Exception e) {
            logger.error("Error creating assignment: ", e);
            throw new RuntimeException("Failed to create assignment: " + e.getMessage());
        }
    }
    
    
    public Assignment getAssignmentById(Long id) {
        Assignment assignment = assignmentDao.findById(id);
        if (assignment == null) {
            throw new RuntimeException("Assignment not found");
        }
        return assignment;
    }
}