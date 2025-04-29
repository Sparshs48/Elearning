package com.example.demo.controller;

import java.time.LocalDateTime;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.config.SessionHelper;
import com.example.demo.config.SessionManager;
import com.example.demo.model.Course;
import com.example.demo.model.Enrollment;
import com.example.demo.model.LoginRequest;
import com.example.demo.model.Submission;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import com.example.demo.service.EnrollmentService;
import com.example.demo.service.SubmissionService;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    
	  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	  @Autowired
	    private SubmissionService submissionService;
	@Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private SessionHelper sessionHelper;

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableCourses(HttpSession session) {
        User student = sessionHelper.getUser(session);
        if (student == null || student.getRole() != UserRole.STUDENT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Student access required");
        }

        try {
            List<Course> availableCourses = enrollmentService.getAvailableCourses(student);
            return ResponseEntity.ok(availableCourses);
        } catch (Exception e) {
            logger.error("Error loading available courses: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error loading courses: " + e.getMessage());
        }
    }

    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<?> enrollInCourse(@PathVariable Long courseId, 
                                          HttpSession session) {
        User student = sessionHelper.getUser(session);
        if (student == null || student.getRole() != UserRole.STUDENT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Student access required");
        }

        try {
            Enrollment enrollment = enrollmentService.enrollStudent(student, courseId);
            return ResponseEntity.ok(enrollment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{enrollmentId}/drop")
    public ResponseEntity<?> dropCourse(@PathVariable Long enrollmentId, HttpSession session) {
        User student = sessionHelper.getUser(session);
        if (student == null || student.getRole() != UserRole.STUDENT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Collections.singletonMap("error", "Student access required"));
        }

        try {
            enrollmentService.dropCourse(enrollmentId, student);
            return ResponseEntity.ok(Collections.singletonMap("message", "Course dropped successfully"));
        } catch (RuntimeException e) {
            logger.error("Error dropping course: ", e);
            return ResponseEntity.badRequest()
                .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

 

    @GetMapping("/student")
    public ResponseEntity<?> getStudentEnrollments(HttpSession session) {
        User student = sessionHelper.getUser(session);
        if (student == null || student.getRole() != UserRole.STUDENT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Student access required");
        }

        try {
            List<Enrollment> enrollments = enrollmentService.getStudentEnrollments(student);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }
//    @GetMapping("/student")
//    public ResponseEntity<?> getStudentEnrollments(HttpSession session) {
//        User student = sessionHelper.getUser(session);
//        if (student == null || student.getRole() != UserRole.STUDENT) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                .body("Student access required");
//        }
//
//        try {
//            List<Enrollment> enrollments = enrollmentService.getStudentEnrollments(student);
//
//            // Fetch submissions and grades for each assignment
//            enrollments.forEach(enrollment -> {
//                if (enrollment.getCourse().getAssignments() != null) {
//                    enrollment.getCourse().getAssignments().forEach(assignment -> {
//                        List<Submission> submissions = submissionService.findByAssignmentAndStudent(
//                            assignment.getId(), student.getId());
//                        if (!submissions.isEmpty()) {
//                            assignment.setLatestSubmission(submissions.get(0)); // Set the most recent submission
//                        }
//                    });
//                }
//            });
//
//            return ResponseEntity.ok(enrollments);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(e.getMessage());
//        }
//    }


}