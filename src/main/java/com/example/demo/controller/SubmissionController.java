package com.example.demo.controller;

import java.time.LocalDateTime;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Path;
import java.nio.file.Paths;



import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
import org.springframework.core.io.UrlResource;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
    @Autowired
    private SubmissionService submissionService;
    
    @Autowired
    private SessionHelper sessionHelper;

    @PostMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> submitAssignment(
            @PathVariable Long assignmentId,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        User student = sessionHelper.getUser(session);
        if (student == null || student.getRole() != UserRole.STUDENT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Student access required");
        }

        try {
            Submission submission = submissionService.submitAssignment(assignmentId, student, file);
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/student")
    public ResponseEntity<?> getStudentSubmissions(HttpSession session) {
        User student = sessionHelper.getUser(session);
        if (student == null || student.getRole() != UserRole.STUDENT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Student access required");
        }

        try {
            List<Submission> submissions = submissionService.getStudentSubmissions(student);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }
    
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> getAssignmentSubmissions(
            @PathVariable Long assignmentId,
            HttpSession session) {
        User professor = sessionHelper.getUser(session);
        if (professor == null || professor.getRole() != UserRole.PROFESSOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Professor access required");
        }

        try {
            List<Submission> submissions = submissionService.getAssignmentSubmissions(assignmentId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

//    @GetMapping("/download/{submissionId}")
//    public ResponseEntity<?> downloadSubmission(@PathVariable Long submissionId, HttpSession session) {
//        User professor = sessionHelper.getUser(session);
//        if (professor == null || professor.getRole() != UserRole.PROFESSOR) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                .body("Professor access required");
//        }
//
//        try {
//            FileSystemResource resource = submissionService.getSubmissionFile(submissionId);
//            return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, 
//                    "attachment; filename=\"" + resource.getFilename() + "\"")
//                .body(resource);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(e.getMessage());
//        }
//    }

    @PutMapping("/{submissionId}/grade")
    public ResponseEntity<?> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestBody Map<String, Object> gradeInfo,
            HttpSession session) {
        User professor = sessionHelper.getUser(session);
        if (professor == null || professor.getRole() != UserRole.PROFESSOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Professor access required");
        }

        try {
            Double grade = Double.valueOf(gradeInfo.get("grade").toString());
            String feedback = (String) gradeInfo.get("feedback");
            
            Submission gradedSubmission = submissionService.gradeSubmission(
                submissionId, grade, feedback);
            return ResponseEntity.ok(gradedSubmission);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }
    
    @GetMapping("/student/grades")
    public ResponseEntity<?> getStudentGradesAndFeedback(HttpSession session) {
        User student = sessionHelper.getUser(session);
        if (student == null || student.getRole() != UserRole.STUDENT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Student access required");
        }

        try {
            List<Submission> submissions = submissionService.getStudentSubmissions(student);
            List<Map<String, Object>> result = submissions.stream().map(submission -> {
                Map<String, Object> map = new HashMap<>();
                map.put("assignmentTitle", submission.getAssignment().getTitle());
                map.put("grade", submission.getGrade());
                map.put("feedback", submission.getFeedback());
                return map;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    @GetMapping("/download/{submissionId}")
    public ResponseEntity<Resource> downloadSubmission(@PathVariable Long submissionId) {
        try {
            // Retrieve the submission details
            Submission submission = submissionService.getSubmissionById(submissionId);

            // Path to the uploaded file
            Path filePath = Paths.get(submissionService.getFilePath(submission));
            Resource fileResource = new UrlResource(filePath.toUri());

            // Check if file exists and is readable
            if (!fileResource.exists() || !fileResource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .build();  // Return 404 without a body
            }

            // Extract the original filename
            String originalFilename = submissionService.getOriginalFilename(submission);

            // Set content type based on file extension
            String contentType = "application/octet-stream"; // Default for unknown types
            if (originalFilename.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (originalFilename.endsWith(".docx")) {
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else if (originalFilename.endsWith(".txt")) {
                contentType = "text/plain";
            }

            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .body(fileResource);

        } catch (Exception e) {
            // Log the exception (optional) for debugging purposes
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();  // Return 500 without a body
        }
    }

}