package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.controller.UserController;
import com.example.demo.dao.CourseDAO;
import com.example.demo.dao.EnrollmentDAO;
import com.example.demo.dao.UserDAO;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.Course;
import com.example.demo.model.Enrollment;
import com.example.demo.model.LoginRequest;
import com.example.demo.model.UserStatus;



@Service
public class EnrollmentService {
    @Autowired
    private EnrollmentDAO enrollmentDAO;
    
    @Autowired
    private CourseDAO courseDAO;
    
	  private static final int DROP_PERIOD_DAYS = 7; // Students can drop within 7 days of enrollment


	  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	  public List<Course> getAvailableCourses(User student) {
		    try {
		        List<Course> allCourses = courseDAO.findAll();
		        if (allCourses.isEmpty()) {
		            logger.info("No courses found in the system");
		            return Collections.emptyList();
		        }
		        
		        List<Course> enrolledCourses = getEnrolledCourses(student);
		        logger.info("Student is enrolled in {} courses", enrolledCourses.size());
		        
		        List<Course> availableCourses = allCourses.stream()
		            .filter(course -> "ACTIVE".equals(course.getStatus()))
		            .filter(course -> !enrolledCourses.contains(course))
		            .filter(course -> course.getMaxStudents() > 0)
		            .collect(Collectors.toList());
		            
		        logger.info("Found {} available courses for student", availableCourses.size());
		        return availableCourses;
		    } catch (Exception e) {
		        logger.error("Error getting available courses: ", e);
		        throw new RuntimeException("Failed to load available courses: " + e.getMessage());
		    }
		}

	  public Enrollment enrollStudent(User student, Long courseId) {
		    // Check if already enrolled in this course
		    if (isAlreadyEnrolled(student, courseId)) {
		        throw new RuntimeException("You are already enrolled in this course");
		    }

		    // Check enrollment limit
		    if (enrollmentDAO.getActiveEnrollmentsCount(student) >= 2) {
		        throw new RuntimeException("Maximum enrollment limit reached (2 courses)");
		    }

		    Course course = courseDAO.findById(courseId);
		    if (course == null) {
		        throw new RuntimeException("Course not found");
		    }

		    // Check available slots
		    if (course.getMaxStudents() <= 0) {
		        throw new RuntimeException("No available slots in this course");
		    }

		    // Decrease available slots
		    course.setMaxStudents(course.getMaxStudents() - 1);
		    courseDAO.save(course);

		    Enrollment enrollment = new Enrollment();
		    enrollment.setStudent(student);
		    enrollment.setCourse(course);
		    enrollment.setEnrollmentDate(LocalDateTime.now());
		    enrollment.setStatus("ACTIVE");
		    
		    return enrollmentDAO.save(enrollment);
		}

	  public void dropCourse(Long enrollmentId, User student) {
	        Enrollment enrollment = enrollmentDAO.findById(enrollmentId);
	        if (enrollment == null || !enrollment.getStudent().getId().equals(student.getId())) {
	            throw new RuntimeException("Invalid enrollment");
	        }
	        
	        // Check if within drop period
	        LocalDateTime dropDeadline = enrollment.getEnrollmentDate().plusDays(DROP_PERIOD_DAYS);
	        if (LocalDateTime.now().isAfter(dropDeadline)) {
	            throw new RuntimeException("Drop period has ended. You can only drop a course within " 
	                + DROP_PERIOD_DAYS + " days of enrollment");
	        }
	        
	        try {
	            enrollmentDAO.dropEnrollment(enrollmentId);
	            Course course = enrollment.getCourse();
	            course.setMaxStudents(course.getMaxStudents() + 1);
	            courseDAO.save(course);
	        } catch (Exception e) {
	            throw new RuntimeException("Failed to drop course: " + e.getMessage());
	        }
	    }

    public List<Enrollment> getStudentEnrollments(User student) {
        return enrollmentDAO.findByStudent(student);
    }
    
    public List<Course> getEnrolledCourses(User student) {
        List<Enrollment> enrollments = enrollmentDAO.findByStudent(student);
        return enrollments.stream()
            .map(Enrollment::getCourse)
            .collect(Collectors.toList());
    }
    
    private boolean isAlreadyEnrolled(User student, Long courseId) {
        return enrollmentDAO.findByStudentAndCourse(student, courseId)
            .stream()
            .anyMatch(e -> "ACTIVE".equals(e.getStatus()));
    }
    
    
}
