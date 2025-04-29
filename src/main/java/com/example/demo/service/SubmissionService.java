package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.controller.UserController;
import com.example.demo.dao.SubmissionDAO;
import com.example.demo.model.Assignment;
import com.example.demo.model.Submission;
import com.example.demo.model.User;
import com.itextpdf.text.pdf.parser.Path;
import com.itextpdf.text.pdf.parser.clipper.Paths;

import jakarta.annotation.Resource;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;



@Service
public class SubmissionService {
	
	  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	
    @Autowired
    private SubmissionDAO submissionDAO;
    
    @Autowired
    private AssignmentService assignmentService;

//    public Submission submitAssignment(Long assignmentId, User student, MultipartFile file) {
//        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
//        
//        // Check if assignment exists
//        if (assignment == null) {
//            throw new RuntimeException("Assignment not found");
//        }
//        
//        // Check submission deadline
//        if (LocalDateTime.now().isAfter(assignment.getDueDate())) {
//            throw new RuntimeException("Assignment submission deadline has passed");
//        }
//        
//        // Save file and get URL
//        String fileUrl = saveSubmissionFile(file);
//        
//        Submission submission = new Submission();
//        submission.setAssignment(assignment);
//        submission.setStudent(student);
//        submission.setFileUrl(fileUrl);
//        submission.setSubmissionDate(LocalDateTime.now());
//        submission.setStatus("SUBMITTED");
//        
//        return submissionDAO.save(submission);
//    }

    private String saveSubmissionFile(MultipartFile file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            // Create absolute path using user.home
            String uploadDir = System.getProperty("user.home") + "/uploads/submissions/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new RuntimeException("Failed to create upload directory");
                }
            }
            
            String filePath = uploadDir + fileName;
            File dest = new File(filePath);
            file.transferTo(dest);
            
            return filePath;
        } catch (IOException e) {
            logger.error("Error saving file: ", e);
            throw new RuntimeException("Failed to save submission file: " + e.getMessage());
        }
    }

    public List<Submission> getStudentSubmissions(User student) {
        return submissionDAO.findByStudent(student);
    }
    
    public List<Submission> getAssignmentSubmissions(Long assignmentId) {
        return submissionDAO.findByAssignmentId(assignmentId);
    }

//    public Resource getSubmissionFile(Long submissionId) {
//        try {
//            Submission submission = submissionDAO.findById(submissionId);
//            if (submission == null) {
//                throw new RuntimeException("Submission not found");
//            }
//
//            Path filePath = Paths.get(submission.getFileUrl());
//            Resource resource = new UrlResource(filePath.toUri());
//            
//            if (!resource.exists() || !resource.isReadable()) {
//                throw new RuntimeException("Could not read the file");
//            }
//            
//            return resource;
//        } catch (Exception e) {
//            throw new RuntimeException("Error accessing file: " + e.getMessage());
//        }
//    }
    
    public FileSystemResource getSubmissionFile(Long submissionId) {
        Submission submission = submissionDAO.findById(submissionId);
        if (submission == null) {
            throw new RuntimeException("Submission not found");
        }

        File file = new File(submission.getFileUrl());
        if (!file.exists()) {
            throw new RuntimeException("File not found");
        }
        
        return new FileSystemResource(file);
    }



    public Submission gradeSubmission(Long submissionId, Double grade, String feedback) {
        if (grade < 0 || grade > 100) {
            throw new RuntimeException("Grade must be between 0 and 100");
        }
        return submissionDAO.updateGrade(submissionId, grade, feedback);
    }
    
    public Submission submitAssignment(Long assignmentId, User student, MultipartFile file) {
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        
        // Check if assignment exists
        if (assignment == null) {
            throw new RuntimeException("Assignment not found");
        }
        
        // Check for existing submission
        List<Submission> existingSubmissions = submissionDAO.findByAssignment(assignment);
        boolean hasSubmitted = existingSubmissions.stream()
            .anyMatch(s -> s.getStudent().getId().equals(student.getId()));
        if (hasSubmitted) {
            throw new RuntimeException("You have already submitted this assignment");
        }
        
        // Check submission deadline
        if (LocalDateTime.now().isAfter(assignment.getDueDate())) {
            throw new RuntimeException("Assignment submission deadline has passed");
        }
        
        // Save file and get URL
        String fileUrl = saveSubmissionFile(file);
        
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setFileUrl(fileUrl);
        submission.setSubmissionDate(LocalDateTime.now());
        submission.setStatus("SUBMITTED");
        
        return submissionDAO.save(submission);
    }
    
    public List<Submission> findByAssignmentAndStudent(Long assignmentId, Long studentId) {
        return submissionDAO.findByAssignmentAndStudent(assignmentId, studentId);
    }
    
    public Submission getSubmissionById(Long submissionId) {
        return submissionDAO.findById(submissionId);
    }

    public String getFilePath(Submission submission) {
        return submission.getFileUrl(); // This maps the file path from your model.
    }

    public String getOriginalFilename(Submission submission) {
        // Extract the original file name from the URL or use logic based on your application.
        String fileUrl = submission.getFileUrl();
        return fileUrl != null ? fileUrl.substring(fileUrl.lastIndexOf('/') + 1) : "unknown_file";
    }


}