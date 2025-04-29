package com.example.demo.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.config.HibernateUtil;
import com.example.demo.controller.UserController;
import com.example.demo.dao.CourseDAO;
import com.example.demo.dao.EnrollmentDAO;
import com.example.demo.model.Course;
import com.example.demo.model.Enrollment;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CourseService {
    @Autowired
    private CourseDAO courseDAO;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EnrollmentDAO enrollmentDAO;

	  private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	  

    
    public Course createCourse(Course course, Long professorId) {
        User professor = userService.getUserById(professorId);
        if (professor == null || professor.getRole() != UserRole.PROFESSOR) {
            throw new RuntimeException("Invalid professor ID");
        }
        
        // Check if professor already has a course
        List<Course> existingCourses = courseDAO.findByProfessor(professor);
        if (!existingCourses.isEmpty()) {
            throw new RuntimeException("Professor already has an assigned course");
        }
        
        course.setProfessor(professor);
        course.setStatus("ACTIVE");
        return courseDAO.save(course);
    }

    public List<Course> getAllCourses() {
        return courseDAO.findAll();
    }

    public Course getCourseById(Long id) {
        return courseDAO.findById(id);
    }
    public Course getCourseByProfessor(User professor) {
        if (professor == null) {
            logger.warn("Null professor provided");
            return null;
        }

        try {
            List<Course> courses = courseDAO.findByProfessor(professor);
            if (courses.isEmpty()) {
                logger.info("No course found for professor: {}", professor.getUsername());
                return null;
            }

            Course course = courses.get(0);
            Hibernate.initialize(course.getAssignments());
            logger.info("Found course: {} for professor: {}", course.getTitle(), professor.getUsername());
            return course;
        } catch (Exception e) {
            logger.error("Error getting course for professor: ", e);
            throw new RuntimeException("Failed to load professor's course");
        }
    }

    public Course updateCourseDescription(User professor, String newDescription) {
        Course course = getCourseByProfessor(professor);
        if (course == null) {
            throw new RuntimeException("No course found for professor");
        }
        
        course.setDescription(newDescription);
        return courseDAO.save(course);
    }
    
    public List<User> getEnrolledStudents(Course course) {
        try {
            if (course == null) {
                logger.warn("Null course provided");
                return Collections.emptyList();
            }
            List<Enrollment> enrollments = enrollmentDAO.findByCourse(course);
            return enrollments.stream()
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .map(Enrollment::getStudent)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting enrolled students: ", e);
            throw new RuntimeException("Failed to load enrolled students");
        }
    }
    
    public List<Course> getAllActiveCourses() {
        try {
            List<Course> courses = courseDAO.findAll();
            return courses.stream()
                .filter(course -> "ACTIVE".equals(course.getStatus()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting active courses: ", e);
            throw new RuntimeException("Failed to load active courses");
        }
    }
    
    public Course refreshCourse(Long courseId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Course course = session.get(Course.class, courseId);
            if (course != null) {
                Hibernate.initialize(course.getAssignments());
            }
            return course;
        } finally {
            if (session != null) session.close();
        }
    }
    
    public Course updateCourse(Course course) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            course = session.merge(course);
            tx.commit();
            return course;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error updating course: ", e);
            throw new RuntimeException("Failed to update course");
        } finally {
            if (session != null) session.close();
        }
    }
    public boolean isStudentEnrolledInCourse(User student, Course course) {
        try {
            List<Enrollment> enrollments = enrollmentDAO.findByStudentAndCourse(student, course);
            return enrollments.stream()
                .anyMatch(e -> "ACTIVE".equals(e.getStatus()));
        } catch (Exception e) {
            logger.error("Error checking student enrollment: ", e);
            throw new RuntimeException("Failed to check student enrollment");
        }
    }

}