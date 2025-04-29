package com.example.demo.controller;

import java.util.Collections;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.config.SessionHelper;
import com.example.demo.model.Assignment;
import com.example.demo.model.Course;
import com.example.demo.model.LectureNote;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.service.AssignmentService;
import com.example.demo.service.CourseService;
import com.example.demo.service.EmailService;
import com.example.demo.service.LectureNoteService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    
	  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	
	  @Autowired
	    private CourseService courseService;
	    
	    @Autowired
	    private SessionHelper sessionHelper;
	    
	    @Autowired
	    private AssignmentService assignmentService;
	    
	    @Autowired
	    private LectureNoteService lectureNoteService;
	    
	    @Autowired
	    private EmailService emailService;

    @PostMapping("/create")
    public ResponseEntity<?> createCourse(@RequestBody Course course, 
                                        @RequestParam Long professorId,
                                        HttpSession session) {
        if (!sessionHelper.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Admin access required");
        }

        try {
            Course createdCourse = courseService.createCourse(course, professorId);
            return ResponseEntity.ok(createdCourse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllCourses(HttpSession session) {
        if (!sessionHelper.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Admin access required");
        }

        try {
            List<Course> courses = courseService.getAllCourses();
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }
    @GetMapping("/professor")
    public ResponseEntity<?> getProfessorCourse(HttpSession session) {
        try {
            User user = sessionHelper.getUser(session);
            if (user == null || user.getRole() != UserRole.PROFESSOR) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Professor access required");
            }

            Course course = courseService.getCourseByProfessor(user);
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error loading course: " + e.getMessage());
        }
    }

    @PutMapping("/professor/description")
    public ResponseEntity<?> updateCourseDescription(
            @RequestBody Map<String, String> payload,
            HttpSession session) {
        User user = sessionHelper.getUser(session);
        if (user == null || user.getRole() != UserRole.PROFESSOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Professor access required");
        }

        try {
            Course course = courseService.updateCourseDescription(
                user, 
                payload.get("description")
            );
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }
    @GetMapping("/{courseId}/description/download")
    public ResponseEntity<?> downloadDescription(@PathVariable Long courseId) {
        try {
            Course course = courseService.getCourseById(courseId);
            if (course == null) {
                return ResponseEntity.notFound().build();
            }

            // Create PDF content
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);

            document.open();
            document.addTitle(course.getTitle());
            document.add(new Paragraph(course.getTitle()));
            document.add(new Paragraph("Description:"));
            document.add(new Paragraph(course.getDescription()));
            document.close();

            byte[] pdfContent = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                course.getTitle().replaceAll("\\s+", "_") + "_description.pdf");

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating PDF: " + e.getMessage());
        }
    }
    
    @PostMapping("/professor/assignments")
    public ResponseEntity<?> createAssignment(@RequestBody Assignment assignment,
                                            HttpSession session) {
        User professor = sessionHelper.getUser(session);
        if (professor == null || professor.getRole() != UserRole.PROFESSOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Assignment created = assignmentService.createAssignment(assignment, professor);
            // Send email notifications to enrolled students
            emailService.notifyStudentsAboutAssignment(created);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/professor/notes/upload")
    public ResponseEntity<?> uploadNote(@RequestParam("file") MultipartFile file,
                                      @RequestParam("title") String title,
                                      HttpSession session) {
        User professor = sessionHelper.getUser(session);
        if (professor == null || professor.getRole() != UserRole.PROFESSOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            LectureNote note = lectureNoteService.uploadNote(file, title, professor);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/professor/students")
    public ResponseEntity<?> getProfessorStudents(HttpSession session) {
        User professor = sessionHelper.getUser(session);
        if (professor == null || professor.getRole() != UserRole.PROFESSOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Course course = courseService.getCourseByProfessor(professor);
            if (course == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<User> students = courseService.getEnrolledStudents(course);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    @GetMapping("/professor/assignments/load")
    public ResponseEntity<?> getProfessorAssignments(HttpSession session) {
        User professor = sessionHelper.getUser(session);
        if (professor == null || professor.getRole() != UserRole.PROFESSOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Course course = courseService.getCourseByProfessor(professor);
            if (course == null) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            return ResponseEntity.ok(course.getAssignments());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }
    
    @GetMapping("/notes/{noteId}/download")
    public ResponseEntity<?> downloadNote(@PathVariable Long noteId, HttpSession session) {
        User student = sessionHelper.getUser(session);
        if (student == null || student.getRole() != UserRole.STUDENT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            LectureNote note = lectureNoteService.getNoteById(noteId);
            if (note == null) {
                return ResponseEntity.notFound().build();
            }

            // Get file path from user.home directory
            String uploadPath = System.getProperty("user.home") + File.separator + "uploads";
            File file = new File(uploadPath, note.getFilePath());

            if (!file.exists()) {
                logger.error("File not found at path: " + file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            FileSystemResource resource = new FileSystemResource(file);
            String originalFileName = note.getFilePath().substring(note.getFilePath().indexOf('_') + 1);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
        } catch (Exception e) {
            logger.error("Error downloading note: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error downloading file: " + e.getMessage());
        }
    }




    @GetMapping("/student/notes")
    public ResponseEntity<?> getEnrolledCourseNotes(HttpSession session) {
        User student = sessionHelper.getUser(session);
        if (student == null || student.getRole() != UserRole.STUDENT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<LectureNote> notes = lectureNoteService.getNotesForStudent(student);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}