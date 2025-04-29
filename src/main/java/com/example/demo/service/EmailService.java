package com.example.demo.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.controller.UserController;
import com.example.demo.model.Assignment;
import com.example.demo.model.User;

@Service
public class EmailService {
    
	  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	
	@Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private CourseService courseService;
	
    public void notifyStudentsAboutAssignment(Assignment assignment) {
        List<User> enrolledStudents = courseService.getEnrolledStudents(assignment.getCourse());
        for(User student : enrolledStudents) {
            sendNotification(
                student.getEmail(),
                "New Assignment Posted",
                "A new assignment '" + assignment.getTitle() + "' has been posted. Due date: " + assignment.getDueDate()
            );
        }
    }
	

    public void sendNotification(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email: ", e);
            // Don't throw exception to prevent assignment creation failure
        }
    }
}