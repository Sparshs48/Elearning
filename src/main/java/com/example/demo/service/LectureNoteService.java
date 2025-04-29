package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dao.CourseDAO;
import com.example.demo.dao.LectureNoteDAO;
import com.example.demo.model.Course;
import com.example.demo.model.LectureNote;
import com.example.demo.model.User;

@Service
public class LectureNoteService {
    @Autowired
    private LectureNoteDAO lectureNoteDAO;
    
    @Autowired
    private CourseDAO courseDAO;
    
    private static final String UPLOAD_DIR = "uploads";

    public LectureNote uploadNote(MultipartFile file, String title, User professor) {
        List<Course> courses = courseDAO.findByProfessor(professor);
        Course professorCourse = courses.isEmpty() ? null : courses.get(0);
        
        if (professorCourse == null) {
            throw new RuntimeException("No course found for professor");
        }

        LectureNote note = new LectureNote();
        note.setTitle(title);
        note.setFilePath(saveFile(file));
        note.setCourse(professorCourse);
        note.setUploadDate(LocalDateTime.now());
        return lectureNoteDAO.save(note);
    }

    private String saveFile(MultipartFile file) {
        try {
            // Create uploads directory in user.home
            String uploadPath = System.getProperty("user.home") + File.separator + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate unique filename
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dest = new File(uploadDir, fileName);
            
            // Transfer file
            file.transferTo(dest);
            
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }
    }
    public LectureNote getNoteById(Long id) {
        LectureNote note = lectureNoteDAO.findById(id);
        if (note == null) {
            throw new RuntimeException("Note not found");
        }
        return note;
    }

    public File getNotePath(LectureNote note) {
        return new File(UPLOAD_DIR, note.getFilePath());
    }

    public List<LectureNote> getNotesForStudent(User student) {
        return lectureNoteDAO.findByCourseInOrderByUploadDateDesc(
            courseDAO.findByEnrolledStudents(student)
        );
    }
}
